package goorm.ddok.global.config;

import goorm.ddok.study.domain.StudyRecruitment;
import goorm.ddok.study.domain.TeamStatus;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StudyStatusUpdateJobConfig {

    private final EntityManagerFactory emf;

    @Bean
    public Job studyStatusUpdateJob(JobRepository repo, Step studyStatusUpdateStep) {
        return new JobBuilder("studyStatusUpdateJob", repo)
                .incrementer(new RunIdIncrementer())
                .start(studyStatusUpdateStep)
                .build();
    }

    @Bean
    public Step studyStatusUpdateStep(JobRepository repo,
                                      PlatformTransactionManager tx,
                                      JpaPagingItemReader<StudyRecruitment> studyStatusReader,
                                      ItemProcessor<StudyRecruitment, StudyRecruitment> studyStatusProcessor,
                                      JpaItemWriter<StudyRecruitment> studyStatusWriter) {
        return new StepBuilder("studyStatusUpdateStep", repo)
                .<StudyRecruitment, StudyRecruitment>chunk(200)
                .reader(studyStatusReader)
                .processor(studyStatusProcessor)
                .writer(studyStatusWriter)
                .transactionManager(tx)
                .build();
    }

    /**
     * 오늘 기준으로 상태 전환 "가능성" 있는 행만 스캔 (삭제 제외, CLOSED 제외, 시작일 도달)
     */
    @Bean
    @StepScope
    public JpaPagingItemReader<StudyRecruitment> studyStatusReader(
            @Value("#{jobParameters['today']}") String todayIso
    ) {
        LocalDate today = (todayIso == null || todayIso.isBlank())
                ? LocalDate.now(ZoneId.of("Asia/Seoul"))
                : LocalDate.parse(todayIso);

        String jpql = """
            SELECT s
              FROM StudyRecruitment s
             WHERE s.deletedAt IS NULL
               AND s.teamStatus <> :closed
               AND s.startDate <= :today
             ORDER BY s.id
            """;

        return new JpaPagingItemReaderBuilder<StudyRecruitment>()
                .name("studyStatusReader")
                .entityManagerFactory(emf)
                .pageSize(200)
                .queryString(jpql)
                .parameterValues(Map.of(
                        "closed", TeamStatus.CLOSED,
                        "today", today
                ))
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<StudyRecruitment, StudyRecruitment> studyStatusProcessor(
            @Value("#{jobParameters['today']}") String todayIso
    ) {
        LocalDate today = (todayIso == null || todayIso.isBlank())
                ? LocalDate.now(ZoneId.of("Asia/Seoul"))
                : LocalDate.parse(todayIso);

        return s -> {
            TeamStatus curr = s.getTeamStatus();

            // RECRUITING → ONGOING
            if (curr == TeamStatus.RECRUITING && !s.getStartDate().isAfter(today)) {
                return s.toBuilder().teamStatus(TeamStatus.ONGOING).build();
            }

            // ONGOING → CLOSED
            if (curr == TeamStatus.ONGOING) {
                int months = (s.getExpectedMonths() == null || s.getExpectedMonths() < 1)
                        ? 1 : s.getExpectedMonths();
                LocalDate endDate = s.getStartDate().plusMonths(months);
                LocalDate cutoff = endDate.plusDays(7);
                if (!today.isBefore(cutoff)) { // today >= cutoff
                    return s.toBuilder().teamStatus(TeamStatus.CLOSED).build();
                }
            }

            // 변경 없음
            return null;
        };
    }

    @Bean
    public JpaItemWriter<StudyRecruitment> studyStatusWriter() {
        return new JpaItemWriterBuilder<StudyRecruitment>()
                .entityManagerFactory(emf)
                .usePersist(false) // merge
                .build();
    }
}
