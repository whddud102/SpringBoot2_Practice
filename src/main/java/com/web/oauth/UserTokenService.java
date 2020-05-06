package com.web.oauth;

import com.web.domain.enums.SocialType;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

import java.util.List;
import java.util.Map;

/**
 *  OAuth2 AccessToken 검증을 위한 클래스,
 *  user 정보를 비동기 통신으로 가져오는 REST Service인 UserInfoTokenServices를 커스터마이징한 클래스
 */
public class UserTokenService extends UserInfoTokenServices {
    // AuthoritiesExtractor를 재설정
    public UserTokenService(ClientResources resources, SocialType socialType) {
        // userInfo를 가져오기 위한 userInfoUri와 cilentID를 파라미터로 전달하여 UserInfoTokenSerivce 객체 생성
        super(resources.getResource().getUserInfoUri(), resources.getClient().getClientId());
        setAuthoritiesExtractor(new OAuth2AuthoritiesExtractor(socialType));
    }
    
    // 권한을 가져오기 위한 AuthoritiesExtractor 구현 클래스를 내부 클래스로 작성
    private class OAuth2AuthoritiesExtractor implements AuthoritiesExtractor {
        private  String socialType;

        public OAuth2AuthoritiesExtractor(SocialType socialType) {
            this.socialType = socialType.getRoleType(); // "ROLE_XXX" 형식으로 권한 생성
        }
    
        // 권한을 리스트 형식으로 생성하여 반환하도록 함
        @Override
        public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
            return AuthorityUtils.createAuthorityList(this.socialType);
        }
    }
}
