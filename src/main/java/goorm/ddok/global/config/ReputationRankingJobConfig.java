package goorm.ddok.global.config;

import goorm.ddok.reputation.batch.ReputationRankingWriter;
import goorm.ddok.reputation.dto.response.TemperatureRankResponse;
import goorm.ddok.reputation.dto.response.TemperatureRegionResponse;
import goorm.ddok.reputation.service.ReputationQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@RequiredArgsConstructor
public class ReputationRankingJobConfig {

    private final ReputationQueryService reputationQueryService;
    private final ReputationRankingWriter writer;

    @Bean
    public Job reputationRankingJob(JobRepository jobRepository, Step computeRankingStep) {
        return new JobBuilder("reputationRankingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(computeRankingStep)
                .build();
    }

    @Bean
    public Step computeRankingStep(JobRepository jobRepository,
                                   PlatformTransactionManager tx,
                                   Tasklet computeRankingTasklet) {
        return new StepBuilder("computeRankingStep", jobRepository)
                .tasklet(computeRankingTasklet, tx)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet computeRankingTasklet(
            @org.springframework.beans.factory.annotation.Value("#{jobParameters['reason']}") String reason,
            @org.springframework.beans.factory.annotation.Value("#{jobParameters['asOfEpochMs']}") Long asOfEpochMs
    ) {
        return (contribution, chunkContext) -> {
            // 필요하면 reason/asOfEpochMs 사용
            var now = java.time.Instant.now();

            var top1 = reputationQueryService.getTop1TemperatureRank(null);
            if (top1 == null || top1.getUserId() == null) {
                writer.writeTop1(null, now);
            } else {
                var patchedTop1 = top1.toBuilder()
                        .IsMine(false)
                        .updatedAt(now)
                        .build();
                writer.writeTop1(patchedTop1, now);
            }

            var top10 = reputationQueryService.getTop10TemperatureRank(null);
            writer.writeTop10(top10, now);

            var regionTop1 = reputationQueryService.getRegionTop1Rank(null);
            writer.writeRegionTop1(regionTop1, now);

            return org.springframework.batch.repeat.RepeatStatus.FINISHED;
        };
    }
}
