package goorm.ddok.reputation.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReputationRankingJobTrigger {

    private final JobLauncher jobLauncher;
    private final Job reputationRankingJob;
    private final JobExplorer jobExplorer;

    /** 서버 시작 시 1회 실행 (기존 @PostConstruct 대체) */
    @EventListener(ApplicationReadyEvent.class)
    public void runOnceOnStartup() {
        triggerInternal("startup");
    }

    /** 매 시간마다 갱신 (기존 cron 유지) */
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void triggerHourly() {
        triggerInternal("hourly");
    }

    private void triggerInternal(String reason) {
        String jobName = reputationRankingJob.getName();
        Set<JobExecution> running = jobExplorer.findRunningJobExecutions(jobName);
        if (!running.isEmpty()) {
            log.warn("Skip {} trigger ({}): already running ({} execs)", jobName, reason, running.size());
            return;
        }
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("asOfEpochMs", System.currentTimeMillis()) // 실행 인스턴스 구분
                    .addString("reason", reason)
                    .toJobParameters();
            JobExecution exec = jobLauncher.run(reputationRankingJob, params);
            log.info("Triggered {} (reason={}) executionId={}", jobName, reason, exec.getId());
        } catch (Exception e) {
            log.error("Failed to run job {}", jobName, e);
        }
    }
}
