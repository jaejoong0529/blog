package kjj.blog;
import kjj.blog.domain.User;
import kjj.blog.domain.UserDto;

// User와 UserDto 간의 변환을 담당하는 유틸리티 클래스
public class UserConverter {

    // User를 UserDto로 변환하는 메서드
    public static UserDto toDto(User user) {
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

    // UserDto를 User로 변환하는 메서드
    public static User fromDto(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setUsername(userDto.getUsername());
        user.setNickname(userDto.getNickname());
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        // dateJoined 및 lastLogin은 엔티티에서 관리하도록 함
        return user;
    }
}
