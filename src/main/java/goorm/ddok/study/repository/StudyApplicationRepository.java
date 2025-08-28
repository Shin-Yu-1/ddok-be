package goorm.ddok.study.repository;

import goorm.ddok.study.domain.StudyApplication;
import goorm.ddok.study.domain.StudyRecruitment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {
    long countByStudyRecruitment(StudyRecruitment study);

    Optional<StudyApplication> findByUser_IdAndStudyRecruitment_Id(Long userId, Long studyId);
}
