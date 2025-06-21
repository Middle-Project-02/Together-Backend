package com.together.server.application.member;

import com.together.server.application.member.request.UpdateMemberInfoRequest;
import com.together.server.application.member.response.MemberInfoResponse;
import com.together.server.application.member.response.UpdateMemberInfoResponse;
import com.together.server.domain.member.Member;
import com.together.server.domain.member.MemberRepository;
import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public MemberInfoResponse getMemberInfo(String id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

        return new MemberInfoResponse(
                member.getMemberId(),
                member.getNickname(),
                Boolean.TRUE.equals(member.getFontMode())
        );
    }

    @Transactional
    public UpdateMemberInfoResponse updateMemberInfo(String memberId, UpdateMemberInfoRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

        if (request.nickname() != null) {
            member.updateNickname(request.nickname());
        }

        if (request.fontMode() != null) {
            member.updateFontMode(request.fontMode());
        }

        return new UpdateMemberInfoResponse(
                member.getMemberId(),
                member.getNickname(),
                member.getFontMode()
        );
    }

    @Transactional
    public void deleteMember(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

        if (member.isDeleted()) {
            throw new CoreException(ErrorType.MEMBER_WITHDRAWN);
        }

        member.delete();
    }
}
