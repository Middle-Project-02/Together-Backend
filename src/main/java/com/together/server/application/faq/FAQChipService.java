package com.together.server.application.faq;

import com.together.server.application.faq.response.FAQChipAnswerResponse;
import com.together.server.application.faq.response.FAQChipResponse;
import com.together.server.domain.faq.FAQChip;
import com.together.server.domain.faq.FAQChipRepository;
import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FAQChipService {

    private final FAQChipRepository faqChipRepository;

    @Transactional(readOnly = true)
    public List<FAQChipResponse> getChips() {
        return faqChipRepository.findAll().stream()
                .map(f -> new FAQChipResponse(f.getId(), f.getQuestion()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FAQChipAnswerResponse getChipAnswer(Long chipId) {
        FAQChip chip = faqChipRepository.findById(chipId)
                .orElseThrow(() -> new CoreException(ErrorType.QUESTION_NOT_FOUND));

        return new FAQChipAnswerResponse(chip.getAnswer());
    }
}
