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
            // Swagger & springdoc
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs.yaml",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            // H2 (개발용)
            "/h2-console/**",
            // Public API
            "/api/auth/signin",
            "/api/auth/signup",
            "/api/auth/signin/kakao",
            "/api/auth/signin/kakao/callback",
            "/api/auth/signin/kakao/token",
            "/api/auth/token",
            "/api/auth/email/find",
            "/api/auth/email/check",
            "/api/auth/email/send-code",
            "/api/auth/phone/send-code",
            "/api/auth/phone/verify-code",
            "/api/auth/password/verify-user",
            "/api/auth/password/reset"
    );


    private static final List<String> GET_PUBLIC_SKIP_PATTERNS = List.of(
            "/api/projects/*",
            "/api/studies/*",
            "/api/players/**",
            "/api/map/**"
    );

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String uri = request.getRequestURI();
        if (WS_SKIP_PATTERNS.stream().anyMatch(p -> PATH_MATCHER.match(p, uri))) {
            log.debug("🧵 Skip JWT filter for WS path: {}", uri);
            return true;
        }
        // Swagger/H2/Public API
        if (PUBLIC_SKIP_PATTERNS.stream().anyMatch(p -> PATH_MATCHER.match(p, uri))) {
            return true;
        }

        return "GET".equalsIgnoreCase(method)
                && GET_PUBLIC_SKIP_PATTERNS.stream().anyMatch(p -> PATH_MATCHER.match(p, uri));
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain chain)
            throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            final String masked = (authHeader == null) ? "null"
                    : (authHeader.length() <= 16 ? "***" : authHeader.substring(0, 16) + "...");

            log.debug("🔐 JWT 필터 - URI: {}, Authorization(masked): {}, QueryTokenPresent:{}",
                    request.getRequestURI(),
                    masked,
                    request.getParameter("token") != null);

            String token = resolveToken(request);
            jwtTokenProvider.validateToken(token);

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);

        } catch (GlobalException ex) {
            SecurityContextHolder.clearContext();
            writeError(response, ex.getErrorCode().getStatus().value(), ex.getErrorCode().getMessage());
        } catch (Exception ex) {
            log.warn("JWT 필터 처리 중 예외", ex);
            SecurityContextHolder.clearContext();
            writeError(response, ErrorCode.INVALID_TOKEN.getStatus().value(), ErrorCode.INVALID_TOKEN.getMessage());
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");

        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        throw new GlobalException(ErrorCode.MISSING_TOKEN);
    }


    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponseDto<?> payload = ApiResponseDto.error(status, message);
        new ObjectMapper().writeValue(response.getWriter(), payload);
    }
}
