package goorm.ddok.global.config;

import goorm.ddok.evaluation.batch.EvaluationCloseTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class EvaluationCloseJobConfig {

    private final EvaluationCloseTasklet evaluationCloseTasklet;

    @Bean
    public Job evaluationCloseJob(JobRepository jobRepository, Step evaluationCloseStep) {
        return new JobBuilder("evaluationCloseJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(evaluationCloseStep)
                .build();
    }

    @Bean
    public Step evaluationCloseStep(JobRepository jobRepository,
                                    PlatformTransactionManager tx) {
        return new StepBuilder("evaluationCloseStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    evaluationCloseTasklet.run(Instant.now());
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, tx)
                .build();
    }
}
