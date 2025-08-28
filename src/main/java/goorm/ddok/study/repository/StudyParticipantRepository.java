package goorm.ddok.study.repository;

import goorm.ddok.study.domain.StudyParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;


public interface StudyParticipantRepository extends JpaRepository<StudyParticipant, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update StudyParticipant p
              set p.deletedAt = :now
            where p.studyRecruitment.id = :studyId
              and p.deletedAt is null
           """)
    int softDeleteByStudyId(@Param("studyId") Long studyId, @Param("now") Instant now);
}
