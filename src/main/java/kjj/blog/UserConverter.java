package kjj.blog;
import kjj.blog.domain.User;
import kjj.blog.domain.UserDto;
import org.springframework.stereotype.Component;

// User와 UserDto 간의 변환을 담당하는 유틸리티 클래스
@Component
//UserConverter와 같은 유틸리티 클래스에 @Component를 사용하는 이유
//자동 빈 등록: 다른 부분에서 쉽게 주입(Injection)할 수 있습니다.
//종속성 주입: UserService에서 UserConverter를 주입받아 사용
public class UserConverter {

    // User엔티티를 UserDto로 변환하는 메서드
    public static UserDto toDto(User user) {
        //return UserDto.builder()
        //                .id(user.getId())
        //                .username(user.getUsername())
        //                .nickname(user.getNickname())
        //                .email(user.getEmail())
        //                .phoneNumber(user.getPhoneNumber())
        //                .dateJoined(user.dateJoinedFormatted())
        //                .lastLogin(user.lastLoginFormatted())
        //                .build();
        //    }
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setNickname(user.getNickname());
        userDto.setEmail(user.getEmail());
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setDateJoined(user.dateJoinedFormatted());
        userDto.setLastLogin(user.lastLoginFormatted());
        return userDto;

    }

    // UserDto를 User엔티티로 변환하는 메서드
    public static User fromDto(UserDto userDto) {
        // return User.builder()
        //                .id(userDto.getId()) // id 필드 추가
        //                .username(userDto.getUsername())
        //                .nickname(userDto.getNickname())
        //                .email(userDto.getEmail())
        //                .phoneNumber(userDto.getPhoneNumber())
        //                .build();
        User user = new User();
        user.setId(userDto.getId());
        user.setUsername(userDto.getUsername());
        user.setNickname(userDto.getNickname());
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        // dateJoined 및 lastLogin은 엔티티에서 관리하도록 함
        //특히 중요한 날짜 및 시간 관련 필드에 대해서는 더 나은 데이터 무결성과 보안을 보장하기위해 엔티티에서 관리
        return user;
    }
    //유저에는 fromdto가 있는 이유
    //사용자 생성 및 업데이트: 클라이언트가 새 사용자 정보를 제출하거나 기존 사용자 정보를 수정할 때,
    // 클라이언트가 제공하는 DTO를 엔티티로 변환하여 데이터베이스에 저장하거나 업데이트해야 합니다.
}
