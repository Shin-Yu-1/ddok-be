package goorm.ddok.study.repository;

import goorm.ddok.study.domain.StudyApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Integer> {
}
