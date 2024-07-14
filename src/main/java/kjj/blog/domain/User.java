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
@Entity
@Getter @Setter
public class User{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String phoneNumber;
    private LocalDateTime dateJoined;
    private LocalDateTime lastLogin;

    public User() {
    }

    public User(String username, String password, String nickname, String phoneNumber, LocalDateTime dateJoined, LocalDateTime lastLogin) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
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
    private String formatted(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}
