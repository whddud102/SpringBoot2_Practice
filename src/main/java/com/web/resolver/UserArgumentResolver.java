package com.web.resolver;

import com.web.annotation.SocialUser;
import com.web.domain.User;
import com.web.domain.enums.SocialType;
import com.web.repository.UserRepository;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.web.domain.enums.SocialType.*;

/**
 * 특정 조건에 해당하는 파라미터가 있으면 생성한 로직을 처리 후, 해당 파라미터에 바인딩 해주는 HandlerMethodArgumentResolver 구현체
 * 인증된 User 객체를 가져오는 역할
 */
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    private UserRepository userRepository;

    public UserArgumentResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        /*
         메소드의 파라미터에 @SocialUser 어노테이션이 표기되어있고,
         파라미터가 User 객체이면 resolveArgument()를 수행하여  파라미터에 값을 바인딩
         */
        return parameter.getParameterAnnotation(SocialUser.class) != null &&
                parameter.getParameterType().equals(User.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // Session에서 인증된 User 객체를 가져와서 반환
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
        User user = (User)session.getAttribute("user");
        return getUser(user, session);
    }

    /*
     Session 에서 가져온 User 객체가 없으면 User 객체를 새로 생성하고, 있으면 바로 사용하도록 반환
     */
    private User getUser(User user, HttpSession session) {
        if(user == null) {  // 가져온 User 객체가 없을 때만 실행
            try {
                OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
                Map<String, Object> map = authentication.getPrincipal().getAttributes();    // 인증 객체에서 개인 정보를 가져옴
                User convertUser = convertUser(authentication.getAuthorizedClientRegistrationId(), map);

                user = userRepository.findByEmail(convertUser.getEmail());
                if (user == null) {
                    user = userRepository.save(convertUser);
                }

                setRoleIfNotSame(user, authentication, map);
                session.setAttribute("user", user);
            } catch (ClassCastException e) {
                return user;
            }

    }
        return user;
    }

    // 인증된 authentication이 권한을 갖고 있는지 체크하는 메서드
    private void setRoleIfNotSame(User user, OAuth2AuthenticationToken authentication, Map<String, Object> map) {
        if(!authentication.getAuthorities().contains(new SimpleGrantedAuthority(user.getSocialType().getRoleType()))) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(map, "N/A",
                    AuthorityUtils.createAuthorityList(user.getSocialType().getRoleType())));
        }
    }

    /**
     * 사용자의 인증된 소셜 미디어 타입에 따라 빌더를 사용하여 User 객체를 만들어주는 메서드
     * @param authority 소셜 미디어 타입
     * @param map 사용자 정보가 들어있는 map
     * @return 소셜 미디어 타입에 따른 User 객체
     */
    private User convertUser(String authority, Map<String, Object> map) {
        if(FACEBOOK.getValue().equals(authority)) return getModernUser(FACEBOOK, map);
        else if(GOOGLE.getValue().equals(authority)) return getModernUser(GOOGLE, map);
        else if (KAKAO.getValue().equals(authority)) return getKakaoUser(map);

        return null;
    }

    /* 
    카카오 회원을 위한 메서드, 타 소셜 미디어와 다른 키의 네이밍을 가짐
    */
    private User getKakaoUser(Map<String,Object> map) {
        //  Map이 object 타입 value를 가지도록 변경되었으므로 value를 String으로 변환
        HashMap<String, String> propertyMap = (HashMap<String, String>)(Object)map.get("properties");
        return User.builder()
                .name(propertyMap.get("nickname"))
                .email(String.valueOf(map.get("kaccount_email")))
                .principal(String.valueOf(map.get("id")))
                .socialType(KAKAO)
                .createdDate(LocalDateTime.now())
                .build();
    }
    
    // Facebook, Google 과 같이 공통되는 명명 규칙을 가진 그룹을 User 객체로 매핑
    private User getModernUser(SocialType socialType, Map<String, Object> map) {
        return User.builder()
                //  Map이 object 타입 value를 가지도록 변경되었으므로 value를 String으로 변환
                .name(String.valueOf(map.get("name")))
                .email(String.valueOf(map.get("email")))
                .principal(String.valueOf(map.get("id")))
                .socialType(socialType)
                .createdDate(LocalDateTime.now())
                .build();
    }


}
