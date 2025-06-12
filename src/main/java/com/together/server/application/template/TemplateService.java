package com.together.server.application.template;

import com.together.server.application.template.request.TemplateSaveRequest;
import com.together.server.application.template.response.TemplateDetailResponse;
import com.together.server.application.template.response.TemplateSimpleResponse;
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
    public void saveTemplate(TemplateSaveRequest request) {
        Template template = new Template(
                request.memberId(),
                request.chatId(),
                request.title(),
                request.content(),
                request.planId()
        );
        templateRepository.save(template);
    }

    @Transactional(readOnly = true)
    public List<TemplateSimpleResponse> getTemplates(Long memberId) {
        return templateRepository.findAllByMemberId(memberId).stream()
                .map(t -> new TemplateSimpleResponse(t.getId(), t.getTitle()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TemplateDetailResponse getTemplateDetail(Long id) {
        Template t = templateRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.TEMPLATE_NOT_FOUND));
        return new TemplateDetailResponse(t.getId(), t.getTitle(), t.getContent(), t.getChatId(), t.getPlanId());
    }

    @Transactional
    public void deleteTemplate(Long templateId, Long memberId) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new CoreException(ErrorType.TEMPLATE_NOT_FOUND));

        if (!template.getMemberId().equals(memberId)) {
            throw new CoreException(ErrorType.FORBIDDEN);
        }

        templateRepository.delete(template);
    }

}
