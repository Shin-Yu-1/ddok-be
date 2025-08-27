package goorm.ddok.study.repository;

import goorm.ddok.study.domain.StudyApplication;
import goorm.ddok.study.domain.StudyRecruitment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {
    long countByStudyRecruitment(StudyRecruitment study);
}
