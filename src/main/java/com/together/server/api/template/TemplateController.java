package com.together.server.api.template;


import com.together.server.application.template.TemplateService;
import com.together.server.application.template.request.TemplateSaveRequest;
import com.together.server.application.template.response.TemplateDetailResponse;
import com.together.server.application.template.response.TemplateSimpleResponse;
import com.together.server.infra.security.Accessor;
import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import com.together.server.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    @Operation(summary = "템플릿 저장", description = "추천 요금제 정보를 템플릿으로 저장")
    public ResponseEntity<ApiResponse<Void>> saveTemplate(@RequestBody TemplateSaveRequest request) {
        templateService.saveTemplate(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{memberId}")
    @Operation(summary = "템플릿 목록 조회", description = "사용자의 템플릿 목록 조회")
    public ResponseEntity<ApiResponse<List<TemplateSimpleResponse>>> listTemplates(@PathVariable Long memberId) {
        List<TemplateSimpleResponse> response = templateService.getTemplates(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/detail/{templateId}")
    @Operation(summary = "템플릿 상세 조회", description = "템플릿 ID로 해당 템플릿 상세 정보 조회")
    public ResponseEntity<ApiResponse<TemplateDetailResponse>> detailTemplate(@PathVariable Long templateId, @AuthenticationPrincipal Accessor accessor) {

        if (accessor.isGuest()) {
            throw new CoreException(ErrorType.FORBIDDEN);
        }

        Long memberId = Long.valueOf(accessor.id());
        TemplateDetailResponse response = templateService.getTemplateDetail(templateId, memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{templateId}")
    @Operation(summary = "템플릿 삭제", description = "템플릿 ID로 해당 템플릿 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long templateId, @AuthenticationPrincipal Accessor accessor) {

        if (accessor.isGuest()) {
            throw new CoreException(ErrorType.FORBIDDEN);
        }

        Long memberId = Long.valueOf(accessor.id());
        templateService.deleteTemplate(templateId, memberId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
