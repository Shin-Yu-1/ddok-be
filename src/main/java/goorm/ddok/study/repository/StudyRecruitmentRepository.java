package goorm.ddok.study.repository;

import goorm.ddok.study.domain.StudyRecruitment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRecruitmentRepository extends JpaRepository<StudyRecruitment, Long> {
}
