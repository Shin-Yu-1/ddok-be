package goorm.ddok.study.repository;

import goorm.ddok.study.domain.StudyParticipant;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StudyParticipantRepository extends JpaRepository<StudyParticipant, Long> {
}
