package kjj.blog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Entity//JPA를 통해 데이터베이스 테이블과 매핑
@Getter @Setter// getter, setter 메서드를 자동으로 생성
public class User{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)//기본 키 생성을 데이터베이스에 위임하는 전략
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phoneNumber;
    private LocalDateTime dateJoined;
    private LocalDateTime lastLogin;
    public User() {//디폴트 생성자
        //디폴트 생성자 필요 이유  1)JPA의 요구 사항  2)프록시 객체 생성   3)스프링과 같은 프레임워크의 호환성
        // 1)JPA는 데이터베이스에서 엔티티 객체를 생성할 때 리플렉션(reflection)을 사용하여 인스턴스를 생성. 이 과정에서 매개변수가 없는 디폴트 생성자가 필요합니다.
        //리플렉션:클래스의 메타데이터를 런타임에 동적으로 분석하고 조작할 수 있는 기능
        //2)JPA에서는 지연 로딩을 지원하기 위해 프록시 객체를 생성할 때,
        // 지연로딩: 데이터베이스와 관련된 ORM(Object-Relational Mapping) 프레임워크에서 자주 사용되는 개념, 객체가 실제로 필요할 때까지 로딩을 지연
        //3)객체를 빈(bean)으로 관리할 때 디폴트 생성자를 사용
    }
    public User(String username, String password, String nickname, String email, String phoneNumber, LocalDateTime dateJoined, LocalDateTime lastLogin) {
        //매개변수 생성자: 사용자 정보를 인자로 받아 초기화하는 생성자
        // 새로운 User 객체를 생성할 때 필요한 모든 정보를 한 번에 설정
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateJoined = dateJoined;
        this.lastLogin = lastLogin;
    }
    /**
     *회원가입 날짜
     */
    public String dateJoinedFormatted() {
        return formatted(dateJoined);
    }

    /**
     *마지막 로그인
     */
    public String lastLoginFormatted() {
        return formatted(lastLogin);
    }


    private String formatted(LocalDateTime dateTime) {//LocalDateTime 객체를 받아서 지정된 형식으로 문자열을 반환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}
