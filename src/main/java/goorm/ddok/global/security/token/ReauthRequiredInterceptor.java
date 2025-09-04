package goorm.ddok.global.security.token;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class ReauthRequiredInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-Reauth-Token";

    private final CustomReauthTokenService customReauthTokenService;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        if (!(handler instanceof HandlerMethod hm)) return true;

        // 메서드나 타입에 @ReauthRequired 있으면 검사
        boolean required = hm.hasMethodAnnotation(ReauthRequired.class)
                || hm.getBeanType().isAnnotationPresent(ReauthRequired.class);

        if (!required) return true;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = auth.getPrincipal();
        Long userId;

        if (principal instanceof goorm.ddok.global.security.auth.CustomUserDetails cud) {
            userId = cud.getId();
        } else {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }

        String token = request.getHeader(HEADER);
        customReauthTokenService.validate(token, userId);
        return true;
    }
}
