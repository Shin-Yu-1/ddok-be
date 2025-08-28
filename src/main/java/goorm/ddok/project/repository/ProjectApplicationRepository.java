package goorm.ddok.project.repository;

import goorm.ddok.project.domain.ProjectApplication;
import goorm.ddok.project.domain.ProjectRecruitment;
import goorm.ddok.project.domain.ProjectRecruitmentPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {

    /**
     * 특정 모집글(ProjectRecruitment)에 대한 전체 지원자 수 조회
     */
    int countByPosition_ProjectRecruitment(ProjectRecruitment recruitment);


    /**
     * 특정 포지션(ProjectRecruitmentPosition)에 대한 지원자 수 조회
     */
    int countByPosition(ProjectRecruitmentPosition position);

    /**
     * 특정 유저(userId)가 특정 모집글(projectId)에 지원했는지 여부 확인
     * - 결과가 있을 수도 있고 없을 수도 있기 때문에 Optional 로 감싸서 반환
     */
    Optional<ProjectApplication> findByUser_IdAndPosition_ProjectRecruitment_Id(Long userId, Long positionId);
}
