package goorm.ddok.global.config;

import goorm.ddok.project.domain.ProjectRecruitment;
import goorm.ddok.project.domain.TeamStatus;
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
public class ProjectStatusUpdateJobConfig {

    private final EntityManagerFactory emf;

    @Bean
    public Job projectStatusUpdateJob(JobRepository repo,
                                      Step projectStatusUpdateStep) {
        return new JobBuilder("projectStatusUpdateJob", repo)
                .incrementer(new RunIdIncrementer())
                .start(projectStatusUpdateStep)
                .build();
    }

    @Bean
    public Step projectStatusUpdateStep(JobRepository repo,
                                        PlatformTransactionManager tx,
                                        JpaPagingItemReader<ProjectRecruitment> projectStatusReader,
                                        ItemProcessor<ProjectRecruitment, ProjectRecruitment> projectStatusProcessor,
                                        JpaItemWriter<ProjectRecruitment> projectStatusWriter) {
        return new StepBuilder("projectStatusUpdateStep", repo)
                .<ProjectRecruitment, ProjectRecruitment>chunk(200)
                .reader(projectStatusReader)
                .processor(projectStatusProcessor)
                .writer(projectStatusWriter)
                .transactionManager(tx)
                .build();
    }

    /**
     * 오늘 기준으로 상태 전환 "가능성" 이 있는 레코드만 읽어 스캔량을 줄임.
     * - RECRUITING && startDate <= :today  (ONGOING 후보)
     * - ONGOING   && startDate <= :today   (CLOSED 후보: endDate 계산은 Processor에서)
     *   (ONGOING 후보를 넉넉히 잡기 위해 startDate <= today 조건만으로 읽고, 실제 전환 여부는 Processor에서 판단)
     */
    @Bean
    @StepScope
    public JpaPagingItemReader<ProjectRecruitment> projectStatusReader(
            @Value("#{jobParameters['today']}") String todayIso
    ) {
        LocalDate today = (todayIso == null || todayIso.isBlank())
                ? LocalDate.now(ZoneId.of("Asia/Seoul"))
                : LocalDate.parse(todayIso);

        String jpql = """
            SELECT p
              FROM ProjectRecruitment p
             WHERE p.deletedAt IS NULL
               AND p.teamStatus <> :closed
               AND p.startDate <= :today
             ORDER BY p.id
            """;

        return new JpaPagingItemReaderBuilder<ProjectRecruitment>()
                .name("projectStatusReader")
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
    public ItemProcessor<ProjectRecruitment, ProjectRecruitment> projectStatusProcessor(
            @Value("#{jobParameters['today']}") String todayIso
    ) {
        LocalDate today = (todayIso == null || todayIso.isBlank())
                ? LocalDate.now(ZoneId.of("Asia/Seoul"))
                : LocalDate.parse(todayIso);

        return p -> {
            TeamStatus current = p.getTeamStatus();

            // RECRUITING -> ONGOING: 시작일 도달(이상)
            if (current == TeamStatus.RECRUITING && !p.getStartDate().isAfter(today)) {
                p = p.toBuilder().teamStatus(TeamStatus.ONGOING).build();
                return p;
            }

            // ONGOING -> CLOSED: endDate(= start + months) + 7일 <= today  (이상)
            if (current == TeamStatus.ONGOING) {
                LocalDate endDate = p.getStartDate().plusMonths(p.getExpectedMonths());
                LocalDate closeCutoff = endDate.plusDays(7);
                if (!today.isBefore(closeCutoff)) { // today >= cutoff
                    p = p.toBuilder().teamStatus(TeamStatus.CLOSED).build();
                    return p;
                }
            }

            // 변경 없음 -> writer로 넘기지 않음
            return null;
        };
    }

    @Bean
    public JpaItemWriter<ProjectRecruitment> projectStatusWriter() {
        return new JpaItemWriterBuilder<ProjectRecruitment>()
                .entityManagerFactory(emf)
                .usePersist(false) // merge 사용
                .build();
    }
}