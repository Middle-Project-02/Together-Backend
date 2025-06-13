package com.together.server.api.faq;

import com.together.server.application.faq.FAQChipService;
import com.together.server.application.faq.response.FAQChipAnswerResponse;
import com.together.server.application.faq.response.FAQChipResponse;
import com.together.server.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/faq-chips")
public class FAQChipController {

    private final FAQChipService faqChipService;

    @GetMapping
    @Operation(summary = "FAQ 칩 목록 조회", description = "자주 묻는 질문 목록 조회")
    public ResponseEntity<ApiResponse<List<FAQChipResponse>>> getChips() {
        return ResponseEntity.ok(ApiResponse.success(faqChipService.getChips()));
    }

    @GetMapping("/{chipId}/answer")
    @Operation(summary = "FAQ 답변 조회", description = "FAQ 칩 ID로 해당 질문의 답변 조회")
    public ResponseEntity<ApiResponse<FAQChipAnswerResponse>> getChipAnswer(@PathVariable Long chipId) {
        return ResponseEntity.ok(ApiResponse.success(faqChipService.getChipAnswer(chipId)));
    }
}
