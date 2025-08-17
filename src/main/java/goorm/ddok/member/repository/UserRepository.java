package goorm.ddok.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import goorm.ddok.member.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
