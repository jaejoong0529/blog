package kjj.blog.repository;

import kjj.blog.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    boolean existsByUsername(String username);
    @Modifying//데이터베이스에 수정, 삭제, 삽입 등의 변경 작업을 수행하는 쿼리 메서드
    @Transactional//메서드 또는 클래스에 트랜잭션 범위를 적용, Modifying을 사용할때 거의 항상 사용
//    @Query("update User u set u.lastLogin = :lastLogin where u.id = :id")//리포지토리 인터페이스의 메서드에 직접 쿼리를 정의합니다.
//    void updateLastLogin(Long id, LocalDateTime lastLogin);
    //컴퓨터로 작업시(intellij)사용하면 파라미터 인식 못하는경우가생겨
    // `@Param`어노테이션에서 사용되는 매개변수의 이름을 지정합니다.
    @Query("update User u set u.lastLogin = :lastLogin where u.id = :id")
    void updateLastLogin(@Param("id") Long id, @Param("lastLogin") LocalDateTime lastLogin);

}
