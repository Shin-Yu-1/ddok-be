package goorm.ddok.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    // SockJS / WebSocket 트랜스포트 경로 및 핸드셰이크 경로는 필터 제외
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> WS_SKIP_PATTERNS = List.of(
            "/ws/**",
            "/ws/chats/**",
            "/sockjs/**",
            "/**/info",
            "/**/websocket",
            "/**/xhr",
            "/**/xhr_send",
            "/**/xhr_streaming",
            "/**/iframe.html"
    );
    private static final List<String> PUBLIC_SKIP_PATTERNS = List.of(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/h2-console/**",
            "/api/map/**",
            "/api/auth/**"
    );

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        boolean skip = WS_SKIP_PATTERNS.stream().anyMatch(p -> PATH_MATCHER.match(p, uri));
        if (skip) {
            log.debug("🧵 Skip JWT filter for WS/SockJS path: {}", uri);
        }

        if (PUBLIC_SKIP_PATTERNS.stream().anyMatch(p -> PATH_MATCHER.match(p, uri))) {
            return true;
        }

        return skip;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain chain)
            throws ServletException, IOException {

        try {
            log.debug("🧪 JWT 필터 실행 - URI: {}, Authorization: {}, QueryToken: {}",
                    request.getRequestURI(),
                    request.getHeader("Authorization"),
                    request.getParameter("token"));

            String uri = request.getRequestURI();

            if (
                    uri.startsWith("/swagger-ui") ||
                    uri.startsWith("/v3/api-docs") ||
                    uri.startsWith("/swagger-resources") ||
                    uri.startsWith("/webjars") ||
                    uri.startsWith("/h2-console") ||
                    uri.startsWith("/api/map/")||


                    // 정확하게 허용할 /api/auth 경로만 명시
                    uri.equals("/api/auth/signin") ||
                    uri.equals("/api/auth/signin/kakao") ||
                    uri.equals("/api/auth/signup") ||
                    uri.equals("/api/auth/email/find") ||
                    uri.equals("/api/auth/email/check") ||
                    uri.equals("/api/auth/password/verify-user") ||
                    uri.equals("/api/auth/password/reset") ||
                    uri.equals("/api/auth/token") ||
                    uri.equals("/api/auth/phone/send-code") ||
                    uri.equals("/api/auth/phone/verify-code") ||
                    uri.equals("/api/auth/email/send-code") ||
                    uri.equals("/api/auth/email/verify") ||
                    uri.equals("/api/auth/signin/kakao/callback") ||
                    uri.equals("/api/auth/signin/kakao/token")

            ) {
                chain.doFilter(request, response);
                return;
            }

            String token = resolveToken(request); // MISSING_TOKEN 발생 가능
            jwtTokenProvider.validateToken(token); // INVALID_TOKEN 발생 가능

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);

        } catch (GlobalException ex) {
            response.setStatus(ex.getErrorCode().getStatus().value());
            response.setContentType("application/json;charset=UTF-8");

            ApiResponseDto<?> errorResponse = ApiResponseDto.error(
                    ex.getErrorCode().getStatus().value(),
                    ex.getErrorCode().getMessage()
            );

            new ObjectMapper().writeValue(response.getWriter(), errorResponse);
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");

        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        throw new GlobalException(ErrorCode.MISSING_TOKEN);
    }
}
