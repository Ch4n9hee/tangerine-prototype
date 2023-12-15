package io.mohajistudio.tangerine.prototype.domain.member.mapper;

import io.mohajistudio.tangerine.prototype.domain.member.domain.Member;
import io.mohajistudio.tangerine.prototype.domain.member.domain.MemberProfile;
import io.mohajistudio.tangerine.prototype.domain.member.dto.MemberDTO;
import io.mohajistudio.tangerine.prototype.domain.member.dto.MemberProfileDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MemberMapper {
    MemberDTO toDTO(Member member);

    MemberProfileDTO toDTO(MemberProfile memberProfile);
}
