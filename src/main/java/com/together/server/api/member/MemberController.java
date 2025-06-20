package com.together.server.api.member;

import com.together.server.application.member.MemberService;
import com.together.server.application.member.request.UpdateMemberInfoRequest;
import com.together.server.application.member.response.MemberInfoResponse;
import com.together.server.application.member.response.UpdateMemberInfoResponse;
import com.together.server.infra.security.Accessor;
import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import com.together.server.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMyInfo(@AuthenticationPrincipal Accessor accessor) {
        MemberInfoResponse response = memberService.getMemberInfo(accessor.id());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/me")
    @Operation(summary = "회원 정보 수정", description = "사용자 닉네임, 요금제, 글씨 크기 수정")
    public ResponseEntity<ApiResponse<UpdateMemberInfoResponse>> updateMemberInfo(
            @AuthenticationPrincipal Accessor accessor,
            @RequestBody UpdateMemberInfoRequest request
    ) {
        if (accessor.isGuest()) {
            throw new CoreException(ErrorType.FORBIDDEN);
        }

        String memberId = accessor.id();
        UpdateMemberInfoResponse response = memberService.updateMemberInfo(memberId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "사용자 탈퇴")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> deleteMember(
            @AuthenticationPrincipal Accessor accessor
    ) {
        if (accessor.isGuest()) {
            throw new CoreException(ErrorType.FORBIDDEN);
        }
        String memberId = accessor.id();
        memberService.deleteMember(memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

