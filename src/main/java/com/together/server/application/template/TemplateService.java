package com.together.server.application.template;

import com.together.server.application.template.request.TemplateSaveRequest;
import com.together.server.application.template.response.TemplateResponse;
import com.together.server.domain.template.Template;
import com.together.server.domain.template.TemplateRepository;
import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;

    @Transactional
    public void saveTemplate(TemplateSaveRequest request, Long memberId) {

        String title = request.title() != null && !request.title().isBlank()
                ? request.title().trim() : "제목 없음";

        String content = request.content() != null && !request.content().isBlank()
                ? request.content().trim() : "내용 없음";

        Template template = new Template(memberId, title, content);
        templateRepository.save(template);
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> getTemplates(Long memberId) {
        return templateRepository.findAllByMemberId(memberId).stream()
                .map(t -> new TemplateResponse(t.getTemplateId(), t.getTitle(), t.getContent()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TemplateResponse getTemplateDetail(Long templateId, Long memberId) {
        Template t = templateRepository.findById(templateId)
                .orElseThrow(() -> new CoreException(ErrorType.TEMPLATE_NOT_FOUND));

        if (!t.getMemberId().equals(memberId)) {
            throw new CoreException(ErrorType.FORBIDDEN);
        }

        return new TemplateResponse(t.getTemplateId(), t.getTitle(), t.getContent());
    }

    @Transactional
    public void deleteTemplate(Long templateId, Long memberId) {
        Template t = templateRepository.findById(templateId)
                .orElseThrow(() -> new CoreException(ErrorType.TEMPLATE_NOT_FOUND));

        if (!t.getMemberId().equals(memberId)) {
            throw new CoreException(ErrorType.FORBIDDEN);
        }

        templateRepository.delete(t);
    }

//    public TemplateSaveRequest parseTitleAndContent(String raw) {
//        String title = "템플릿 제목";
//        String content = raw;
//
//        int titleStart = raw.indexOf("제목:");
//        int contentStart = raw.indexOf("내용:");
//
//        if (titleStart != -1 && contentStart != -1 && titleStart < contentStart) {
//            String extractedTitle = raw.substring(titleStart + 3, contentStart).trim();
//            String extractedContent = raw.substring(contentStart + 3).trim();
//            if (!extractedTitle.isEmpty()) title = extractedTitle;
//            if (!extractedContent.isEmpty()) content = extractedContent;
//        }
//
//        return new TemplateSaveRequest(title, content);
//    }
}
