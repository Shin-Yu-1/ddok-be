package goorm.ddok.global.config;

import io.sentry.Sentry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SentryConfig {

    @Value("${sentry.dsn}")
    private String sentryDsn;

    @Value("${sentry.environment:development}")
    private String sentryEnvironment;

    @Value("${sentry.release:1.0.0}")
    private String sentryRelease;

    @Value("${sentry.debug:true}")
    private boolean sentryDebug;

    @Value("${sentry.traces-sample-rate:1.0}")
    private double sentryTracesSampleRate;

    @PostConstruct
    public void initSentryManually() {
        try {
            log.info("🔧 Sentry 수동 초기화 시작...");

            Sentry.init(options -> {
                options.setDsn(sentryDsn);
                options.setEnvironment(sentryEnvironment);
                options.setRelease(sentryRelease);
                options.setDebug(sentryDebug);
                options.setTracesSampleRate(sentryTracesSampleRate);
                options.setAttachStacktrace(false);
                options.setAttachThreads(false);
                options.setTracesSampleRate(0.1);

                // 성능 모니터링은 traces-sample-rate > 0 이면 자동으로 활성화됨
                log.info("🔧 Sentry 옵션 설정 완료");
            });

            if (Sentry.isEnabled()) {
                log.info("✅ Sentry 초기화 성공!");
                log.info("  - Environment: {}", sentryEnvironment);
                log.info("  - Release: {}", sentryRelease);
                log.info("  - Debug: {}", sentryDebug);
                log.info("  - Traces Sample Rate: {}", sentryTracesSampleRate);
            } else {
                log.error("❌ Sentry 초기화 실패");
            }

        } catch (Exception e) {
            log.error("❌ Sentry 초기화 중 예외 발생: {}", e.getMessage(), e);
        }
    }
}
