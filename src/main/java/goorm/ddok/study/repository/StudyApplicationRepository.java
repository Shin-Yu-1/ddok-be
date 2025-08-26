package goorm.ddok.study.repository;

import goorm.ddok.study.domain.StudyApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {

    Optional<StudyApplication> findByUser_IdAndStudyRecruitment_Id(Long userId, Long studyId);
}
