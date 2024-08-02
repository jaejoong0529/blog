package kjj.blog.controller;

import jakarta.servlet.http.HttpSession;
import kjj.blog.UserConverter;
import kjj.blog.domain.User;
import kjj.blog.domain.UserDto;
import kjj.blog.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
//리펙토링해서 서비스로 나눈 컨트롤러
//추가로 로그랑 UserConverter 헬퍼 클래스생성
//@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserControllerV3 {

    private final UserService userService;

    public UserControllerV3(UserService userService) {
        this.userService = userService;
    }

    // 사용자 목록을 가져오는 메서드
    @GetMapping
    public ResponseEntity<List<UserDto>> list(@RequestParam(name = "page", defaultValue = "0") int page) {
        log.info("Fetching user list for page: {}", page); // 로그 추가
        Pageable pageable = PageRequest.of(page, 10);
        Page<User> users = userService.findAllUsers(pageable);
        List<UserDto> userDTOs = users.getContent().stream()
                .map(UserConverter::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    // 회원 가입을 처리하는 메서드
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody UserDto userDto) {
        log.info("Attempting to register user with username: {}", userDto.getUsername()); // 로그 추가
        if (userService.userExistsByUsername(userDto.getUsername())) {
            log.warn("Username already exists: {}", userDto.getUsername()); // 경고 로그 추가
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }
        userService.registerUser(userDto);
        log.info("User registered successfully: {}", userDto.getUsername()); // 로그 추가
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    // 로그인 처리를 위한 메서드
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam("username") String username,
                                        @RequestParam("password") String password,
                                        HttpSession session) {
        log.info("Attempting to log in with username: {}", username); // 로그 추가
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isPresent() && userService.passwordMatches(password, userOptional.get().getPassword())) {
            User user = userOptional.get();
            session.setAttribute("user", user);
            user.setLastLogin(LocalDateTime.now());
            userService.updateUserLastLogin(user.getId(), user.getLastLogin());
            log.info("Login successful for username: {}", username); // 로그 추가
            return ResponseEntity.ok("Login successful");
        } else {
            log.warn("Invalid username or password for username: {}", username); // 경고 로그 추가
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    // 로그아웃 처리를 위한 메서드
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        log.info("User logged out successfully"); // 로그 추가
        return ResponseEntity.ok("Logout successful");
    }

    // 사용자 삭제를 처리하는 메서드
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        log.info("Attempting to delete user with ID: {}", id); // 로그 추가
        try {
            userService.deleteUser(id);
            log.info("User deleted successfully with ID: {}", id); // 로그 추가
            return ResponseEntity.ok("User deleted successfully");
        } catch (DataIntegrityViolationException e) {
            log.error("Error deleting user with ID: {}: {}", id, e.getMessage()); // 에러 로그 추가
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot delete user with existing posts. Please delete posts first.");
        }
    }

    // 현재 로그인한 사용자의 프로필을 가져오는 메서드
    @GetMapping("/profile")
    public ResponseEntity<UserDto> profile(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            log.info("Fetching profile for username: {}", user.getUsername()); // 로그 추가
            return ResponseEntity.ok(UserConverter.toDto(user));
        } else {
            log.warn("Unauthorized access attempt to profile"); // 경고 로그 추가
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // 이메일을 통해 사용자 이름을 찾는 메서드
    @PostMapping("/find-username")
    public ResponseEntity<String> findUsername(@RequestParam("email") String email) {
        log.info("Attempting to find username for email: {}", email); // 로그 추가
        Optional<User> userOptional = userService.findUserByEmail(email);
        if (userOptional.isPresent()) {
            userService.sendUsernameEmail(email, userOptional.get().getUsername());
            log.info("Username sent to email: {}", email); // 로그 추가
            return ResponseEntity.ok("Username has been sent to your email");
        } else {
            log.warn("User not found with email: {}", email); // 경고 로그 추가
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with this email");
        }
    }
}

