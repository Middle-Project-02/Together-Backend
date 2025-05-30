package com.together.server.api.member;

import com.together.server.application.member.MemberService;
import com.together.server.application.member.response.MemberInfoResponse;
import com.together.server.infra.security.Accessor;
import com.together.server.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

