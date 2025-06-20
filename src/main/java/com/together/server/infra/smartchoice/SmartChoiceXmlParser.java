package com.together.server.infra.smartchoice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.*;

@Component
@Slf4j
public class SmartChoiceXmlParser {

    public List<Map<String, String>> parsePlans(String xml) {
        List<Map<String, String>> result = new ArrayList<>();

        try {
            // XML 전처리 - BOM 제거 및 트림
            String cleanedXml = xml.trim();
            if (cleanedXml.startsWith("\uFEFF")) {
                cleanedXml = cleanedXml.substring(1);
            }

            log.info("파싱할 XML 앞 200자: {}", cleanedXml.substring(0, Math.min(200, cleanedXml.length())));

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(cleanedXml.getBytes("UTF-8")));
            doc.getDocumentElement().normalize();

            log.info("XML 루트 요소: {}", doc.getDocumentElement().getNodeName());

            NodeList itemList = doc.getElementsByTagName("item");
            log.info("찾은 item 개수: {}", itemList.getLength());

            for (int i = 0; i < itemList.getLength(); i++) {
                Node itemNode = itemList.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    Map<String, String> planData = new HashMap<>();

                    planData.put("telecom", getTagValue("v_tel", element));
                    planData.put("planName", getTagValue("v_plan_name", element));
                    planData.put("price", getTagValue("v_plan_price", element));
                    planData.put("voice", getTagValue("v_plan_display_voice", element));
                    planData.put("data", getTagValue("v_plan_display_data", element));
                    planData.put("sms", getTagValue("v_plan_display_sms", element));

                    log.info("파싱된 요금제 {}: {}", i, planData);
                    result.add(planData);
                }
            }

        } catch (Exception e) {
            log.error("SmartChoice XML 파싱 실패 - XML 내용: {}", xml, e);
        }

        return result;
    }

    private String getTagValue(String tag, Element element) {
        try {
            NodeList nodeList = element.getElementsByTagName(tag);
            if (nodeList.getLength() == 0) return "";
            Node node = nodeList.item(0);
            if (node == null || node.getFirstChild() == null) return "";
            return node.getFirstChild().getNodeValue();
        } catch (Exception e) {
            log.warn("태그 {} 파싱 실패", tag, e);
            return "";
        }
    }
}