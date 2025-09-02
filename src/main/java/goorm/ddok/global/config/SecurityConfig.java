package goorm.ddok.global.config;

import org.springframework.http.HttpMethod;
import goorm.ddok.global.security.jwt.JwtAuthenticationFilter;
import goorm.ddok.global.security.jwt.JwtTokenProvider;
import goorm.ddok.global.util.sentry.SentryUserContextFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final SentryUserContextFilter sentryUserContextFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 개발/운영 환경별 허용 도메인 설정
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",     // 개발환경 (모든 포트)
                "https://api.ddok.site"   // 운영환경
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // 허용할 헤더
        configuration.setAllowedHeaders(List.of("*")); // 모든 헤더 허용

        // 노출할 헤더 (클라이언트에서 접근 가능한 헤더)
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "Set-Cookie",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));

        // 쿠키/인증 정보 허용
        configuration.setAllowCredentials(true);

        // 프리플라이트 요청 캐시 시간
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        final String[] SWAGGER_WHITELIST = {
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/v3/api-docs.yaml",
                "/swagger-resources/**",
                "/webjars/**"
        };

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/auth/signout")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/auth/preferences")).authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/projects/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/studies/*").permitAll()
                        .requestMatchers(HttpMethod.GET , "/api/players/**").permitAll()
                        .requestMatchers(HttpMethod.GET , "/api/map/**").permitAll()
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
                        .requestMatchers(
                                new AntPathRequestMatcher("/h2-console/**"),
                                new AntPathRequestMatcher("/api/auth/**"),
                                new AntPathRequestMatcher("/ws/**"),
                                new AntPathRequestMatcher("/ws/chats/**"),
                                new AntPathRequestMatcher("/**/info"),
                                new AntPathRequestMatcher("/**/websocket"),
                                new AntPathRequestMatcher("/**/xhr_streaming"),
                                new AntPathRequestMatcher("/**/xhr_send"),
                                new AntPathRequestMatcher("/**/xhr"),
                                new AntPathRequestMatcher("/**/iframe.html")
                        ).permitAll()
                        .requestMatchers("/oauth/kakao", "/oauth/kakao/callback", "/api/auth/signin/kakao").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterAfter(sentryUserContextFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
