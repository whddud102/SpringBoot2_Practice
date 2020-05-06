package com.web.resolver;

import com.web.annotation.SocialUser;
import com.web.domain.User;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpSession;

/**
 * 특정 조건에 해당하는 파라미터가 있으면 생성한 로직을 처리 후, 해당 파라미터에 바인딩 해주는 HandlerMethodArgumentResolver 구현체
 * 인증된 User 객체를 가져오는 역할
 */
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

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

    private User getUser(User user, HttpSession session) {
    }
}
