package goorm.ddok.study.batch;

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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyStatusUpdateJobTrigger {

    private final JobLauncher jobLauncher;
    private final Job studyStatusUpdateJob;
    private final JobExplorer jobExplorer;

    @EventListener(ApplicationReadyEvent.class)
    public void runOnceAtStartup() {
        triggerInternal("startup");
    }

    // 매일 00:00 (KST)
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void triggerDaily() {
        triggerInternal("daily-midnight");
    }

    private void triggerInternal(String reason) {
        String name = studyStatusUpdateJob.getName();
        Set<JobExecution> running = jobExplorer.findRunningJobExecutions(name);
        if (!running.isEmpty()) {
            log.warn("Skip {} trigger ({}): already running ({})", name, reason, running.size());
            return;
        }
        try {
            String today = LocalDate.now(ZoneId.of("Asia/Seoul")).toString();
            JobParameters params = new JobParametersBuilder()
                    .addLong("ts", System.currentTimeMillis())
                    .addString("today", today)
                    .addString("reason", reason)
                    .toJobParameters();
            JobExecution exec = jobLauncher.run(studyStatusUpdateJob, params);
            log.info("Triggered {} (reason={}) execId={}", name, reason, exec.getId());
        } catch (Exception e) {
            log.error("Failed to run {}", name, e);
        }
    }
}
