package kjj.blog.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phoneNumber;
    private String dateJoined;
    private String lastLogin;
}
//DTO(Data Transfer Object) : 클라이언트와 서버 간 데이터 전송을 위해 설계된 객체
//Entity : 데이터베이스에 저장되는 데이터 객체로, 데이터베이스와 직접적으로 연결
//컨트롤러에서는 DTO의 형태로 데이터를 받아 서비스에 전달한다.
//서비스에서는 컨트롤러에서 받은 DTO를 Entity로 변환하고, 필요한 작업을 수행한 뒤에 Repository에 Entity를 전달한다.
//toEntity(), toDto() 같은 메서드를 DTO 클래스에 직접 변환 메서드를 구현하여 상호변환을 수행할 수 있다.
