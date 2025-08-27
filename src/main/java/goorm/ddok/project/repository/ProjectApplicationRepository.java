package goorm.ddok.project.repository;

import goorm.ddok.project.domain.ApplicationStatus;
import goorm.ddok.project.domain.ProjectApplication;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {

    Optional<ProjectApplication> findByUser_IdAndPosition_ProjectRecruitment_Id(Long userId, Long positionId);

    // 이미 쓰고 있는 카운트 쿼리/프로젝션들은 그대로 두고,
    // 삭제용만 추가
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ProjectApplication a where a.position.projectRecruitment.id = :projectId")
    void deleteByPosition_ProjectRecruitment_Id(@Param("projectId") Long projectId);

    // (참고) 서비스에서 쓰던 존재 여부/카운트 메서드가 이미 있다면 유지
    boolean existsByUser_IdAndPosition_ProjectRecruitment_Id(Long userId, Long projectId);
    boolean existsByUser_IdAndPosition_ProjectRecruitment_IdAndStatus(Long userId, Long projectId, ApplicationStatus status);
}


