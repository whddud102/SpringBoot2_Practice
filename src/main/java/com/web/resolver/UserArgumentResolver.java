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
import org.springframework.security.oauth2.provider.OAuth2Authentication;
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
        // 파라미터의 어노테이션과 타입을 검사
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
                OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
                Map<String, String> map = (HashMap<String, String>) authentication.getUserAuthentication().getDetails();
                User convertUser = convertUser(String.valueOf(authentication.getAuthorities().toArray()[0]), map);

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
    private void setRoleIfNotSame(User user, OAuth2Authentication authentication, Map<String, String> map) {
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
    private User convertUser(String authority, Map<String, String> map) {
        if(FACEBOOK.isEquals(authority)) return getModernUser(FACEBOOK, map);
        else if(GOOGLE.isEquals(authority)) return getModernUser(GOOGLE, map);
        else if (KAKAO.isEquals(authority)) return getKakaoUser(map);

        return null;
    }

    /* 
    카카오 회원을 위한 메서드, 타 소셜 미디어와 다른 키의 네이밍을 가짐
    */
    private User getKakaoUser(Map<String, String> map) {
        HashMap<String, String> propertyMap = (HashMap<String, String>)(Object)map.get("properties");
        return User.builder()
                .name(propertyMap.get("nickname"))
                .email(map.get("kaccount_email"))
                .principal(String.valueOf(map.get("id")))
                .socialType(KAKAO)
                .createdDate(LocalDateTime.now())
                .build();
    }
    
    // Facebook, Google 과 같이 공통되는 명명 규칙을 가진 그룹을 User 객체로 매핑
    private User getModernUser(SocialType socialType, Map<String, String> map) {
        return User.builder()
                .name(map.get("name"))
                .email(map.get("email"))
                .principal(map.get("id"))
                .socialType(socialType)
                .createdDate(LocalDateTime.now())
                .build();
    }


}
