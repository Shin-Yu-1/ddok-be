package goorm.ddok.member.repository;

import goorm.ddok.member.domain.UserTechStack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserTechStackRepository extends JpaRepository<UserTechStack, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserTechStack uts where uts.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    boolean existsByUser_IdAndTechStack_Id(Long userId, Long techStackId);
}