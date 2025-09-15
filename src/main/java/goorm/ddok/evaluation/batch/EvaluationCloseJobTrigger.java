package goorm.ddok.evaluation.batch;

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
public class EvaluationCloseJobTrigger {

    private final JobLauncher jobLauncher;
    private final Job evaluationCloseJob;
    private final JobExplorer jobExplorer;

    @EventListener(ApplicationReadyEvent.class)
    public void runOnceAtStartup() { triggerInternal("startup"); }

    // 매일 자정(Asia/Seoul)
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void triggerDailyMidnight() { triggerInternal("daily-midnight"); }

    private void triggerInternal(String reason) {
        var name = evaluationCloseJob.getName();
        Set<JobExecution> running = jobExplorer.findRunningJobExecutions(name);
        if (!running.isEmpty()) {
            log.warn("Skip {} trigger ({}): already running {}", name, reason, running.size());
            return;
        }
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("ts", System.currentTimeMillis())
                    .addString("reason", reason)
                    .toJobParameters();
            JobExecution exec = jobLauncher.run(evaluationCloseJob, params);
            log.info("Triggered {} (reason={}) execId={}", name, reason, exec.getId());
        } catch (Exception e) {
            log.error("Failed to run {}", name, e);
        }
    }
}
