package com.together.server.domain.plan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmartChoicePlan {
    private String planName;
    private String data;
    private String voice;
    private String sms;
    private String telecom;
    private String price;

    public static SmartChoicePlan from(Map<String, String> map) {
        return new SmartChoicePlan(
                map.getOrDefault("planName", ""),
                map.getOrDefault("data", ""),
                map.getOrDefault("voice", ""),
                map.getOrDefault("sms", ""),
                map.getOrDefault("telecom", ""),
                map.getOrDefault("price", "0")
        );
    }

    public int getPrice() {
        try {
            return Integer.parseInt(this.price);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    public static SmartChoicePlan empty() {
        return new SmartChoicePlan("추천 요금제가 없습니다", "-", "-", "-", "-", "0");
    }
}
