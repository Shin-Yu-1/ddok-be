package goorm.ddok.member.repository;

import goorm.ddok.member.domain.UserTechStack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserTechStackRepository extends JpaRepository<UserTechStack, Long> {

    Optional<UserTechStack> findByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserTechStack uts where uts.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    boolean existsByUser_IdAndTechStack_Id(Long userId, Long techStackId);

    // 다건 + 페이징 조회용
    Page<UserTechStack> findByUserId(Long userId, Pageable pageable);
}
