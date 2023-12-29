package io.mohajistudio.tangerine.prototype.global.auth.service;

import io.jsonwebtoken.*;
import io.mohajistudio.tangerine.prototype.global.auth.domain.SecurityMember;
import io.mohajistudio.tangerine.prototype.global.config.JwtProperties;
import io.mohajistudio.tangerine.prototype.global.auth.dto.GeneratedToken;
import io.mohajistudio.tangerine.prototype.domain.member.domain.Member;
import io.mohajistudio.tangerine.prototype.global.error.exception.BusinessException;
import io.mohajistudio.tangerine.prototype.domain.member.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.*;

import static io.mohajistudio.tangerine.prototype.global.enums.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtProperties jwtConfig;
    private final MemberRepository memberRepository;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private Key signingKey;
    private JwtParser jwtParser;
//    private static final Long ACCESS_TOKEN_PERIOD = 1000L * 60L * 60L; // 1시간
    private static final Long ACCESS_TOKEN_PERIOD = 1000L * 60L * 60L * 24L * 14L; // 2주
    private static final Long REFRESH_TOKEN_PERIOD = 1000L * 60L * 60L * 24L * 14L; // 2주
    private static final short REFRESH_TOKEN_EXPIRATION_THRESHOLD_DAYS = 7;

    @PostConstruct
    protected void init() {
        String secretKey = Base64.getEncoder().encodeToString(jwtConfig.getSecretKey().getBytes());
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secretKey);
        signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
        jwtParser = Jwts.parserBuilder().setSigningKey(signingKey).build();
    }

    @Transactional
    public GeneratedToken generateTokens(Long id, String email, String provider, String role) {
        String accessToken = generateToken(id, email, provider, role, ACCESS_TOKEN_PERIOD);
        String refreshToken = generateToken(id, email, provider, role, REFRESH_TOKEN_PERIOD);

        saveRefreshToken(id, refreshToken);

        return GeneratedToken.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    public GeneratedToken generateTokens(SecurityMember securityMemberDTO) {
        String accessToken = generateToken(securityMemberDTO, ACCESS_TOKEN_PERIOD);
        String refreshToken = generateToken(securityMemberDTO, REFRESH_TOKEN_PERIOD);

        saveRefreshToken(securityMemberDTO.getId(), refreshToken);

        return GeneratedToken.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    public GeneratedToken generateGuestToken(Long id, String email, String provider, String role) {
        String accessToken = generateToken(id, email, provider, role, ACCESS_TOKEN_PERIOD);

        return GeneratedToken.builder().accessToken(accessToken).build();
    }

    private String generateToken(Long id, String email, String provider, String role, Long tokenPeriod) {
        Claims claims = Jwts.claims().setSubject("id");
        claims.put("role", role);
        claims.put("provider", provider);
        claims.put("email", email);
        claims.setId(String.valueOf(id));
        Date now = new Date();

        return Jwts.builder().setClaims(claims).setIssuedAt(now).setExpiration(new Date(now.getTime() + tokenPeriod)).signWith(signingKey, signatureAlgorithm).compact();
    }

    private String generateToken(SecurityMember securityMemberDTO, Long tokenPeriod) {
        Claims claims = Jwts.claims().setSubject("id");
        claims.put("role", securityMemberDTO.getRole().name());
        claims.put("provider", securityMemberDTO.getProvider().name());
        claims.put("email", securityMemberDTO.getEmail());
        claims.setId(String.valueOf(securityMemberDTO.getId()));
        Date now = new Date();

        return Jwts.builder().setClaims(claims).setIssuedAt(now).setExpiration(new Date(now.getTime() + tokenPeriod)).signWith(signingKey, signatureAlgorithm).compact();
    }

    @Transactional
    public GeneratedToken reissueToken(String refreshToken) {
        GeneratedToken generatedTokenDTO;
        String reissuedRefreshToken = null;
        String reissuedAccessToken;
        Claims claims = verifyToken(refreshToken);
        SecurityMember securityMemberDTO = SecurityMember.fromClaims(claims);

        Optional<Member> findMember = memberRepository.findById(securityMemberDTO.getId());

        if(findMember.isEmpty()) {
            throw new BusinessException(MEMBER_NOT_FOUND);
        }

        Member member = findMember.get();

        if(member.getRefreshToken() == null) {
            throw new BusinessException(MISMATCH_REFRESH_TOKEN);
        }

        if(!member.getRefreshToken().equals(refreshToken)) {
            throw new BusinessException(MISMATCH_REFRESH_TOKEN);
        }

        long remainingTokenExpiration = calculateRemainingTokenExpirationInMilliseconds(claims);

        if (remainingTokenExpiration < REFRESH_TOKEN_EXPIRATION_THRESHOLD_DAYS) {
            reissuedRefreshToken = generateToken(securityMemberDTO, REFRESH_TOKEN_PERIOD);
        }

        reissuedAccessToken = generateToken(securityMemberDTO, ACCESS_TOKEN_PERIOD);
        generatedTokenDTO = GeneratedToken.builder().accessToken(reissuedAccessToken).refreshToken(reissuedRefreshToken).build();

        return generatedTokenDTO;
    }

    long calculateRemainingTokenExpirationInMilliseconds(Claims claims) {
        long tokenExpiration = claims.getExpiration().getTime();
        long nowDateToMilliseconds = new Date().getTime();
        return ((long) Math.floor(tokenExpiration - nowDateToMilliseconds) / 1000L / 60L / 60L / 24L);
    }

    public Claims verifyToken(String token) {
        try {
            return jwtParser.parseClaimsJws(token).getBody();
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new JwtException("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            throw new JwtException("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new JwtException("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new JwtException("잘못된 JWT 토큰입니다.");
        }
    }

    private void saveRefreshToken(Long id, String refreshToken) {
        Optional<Member> findMember = memberRepository.findById(id);
        findMember.ifPresent(member -> memberRepository.updateRefreshToken(member.getId(), refreshToken));
    }
}
