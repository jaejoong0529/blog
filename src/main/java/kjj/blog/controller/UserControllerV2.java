package kjj.blog.controller;

// REST API를 통한 JSON 데이터를 주고받는 방식
//JSON 기반 REST API로 변환하기
//HTML 뷰 대신 JSON 반환: 컨트롤러 메서드는 JSON 데이터를 반환합니다.
//@RestController 사용: @Controller 대신 @RestController를 사용하여 메서드들이 JSON을 반환하도록 합니다.
//HTTP 상태 코드 사용: 적절한 HTTP 상태 코드를 사용하여 요청 결과를 나타냅니다.
//DTO 사용: Data Transfer Object(DTO)를 사용하여 엔티티와 직접적으로 데이터를 주고받는 것을 피합니다. 이는 보안성과 데이터 무결성을 위해 중요합니다.
//응답 형식 통일: 모든 API가 통일된 응답 형식(JSON)을 반환하도록 합니다.

import jakarta.servlet.http.HttpSession;
import kjj.blog.domain.User;
import kjj.blog.domain.UserDto;
import kjj.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserControllerV2 {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    // 유저 목록 조회
    @GetMapping
    public ResponseEntity<List<UserDto>> list(@RequestParam(name = "page", defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<User> users = userRepository.findAll(pageable);
        List<UserDto> userDTOs = users.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody UserDto userDto) {
        boolean exists = userRepository.existsByUsername(userDto.getUsername());
        if (!exists) {
            User user = new User();
            user.setUsername(userDto.getUsername());
            user.setPassword(passwordEncoder.encode(userDto.getPassword())); // 비밀번호 암호화
            user.setNickname(userDto.getNickname());
            user.setEmail(userDto.getEmail());
            user.setPhoneNumber(userDto.getPhoneNumber());
            user.setDateJoined(LocalDateTime.now());
            user.setLastLogin(user.getDateJoined());
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }
    }
    //postman에서 테스트 raw json, body에{
    //    "username": "testuser",
    //    "password": "password123!",
    //    "nickname": "Test User",
    //    "email": "testuser@example.com",
    //    "phoneNumber": "123-456-7890"
    //}

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam("username") String username,
                                        @RequestParam("password") String password,
                                        HttpSession session) {
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            session.setAttribute("user", user);
            user.setLastLogin(LocalDateTime.now());
            userRepository.updateLastLogin(user.getId(), user.getLastLogin());
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }
    //Type: x-www-form-urlencoded
    //Key-Value:
    //username: testuser
    //password: password123!

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logout successful");
    }
    //post로 url만 입력

    // 사용자 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            userRepository.deleteById(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot delete user with existing posts. Please delete posts first.");
        }
    }
    //Method: DELETE
    //
    //URL: http://localhost:8080/{id} (여기서 {id}는 실제 사용자 ID로 대체해야 합니다.)
    //
    //Body: 빈 내용으로 DELETE 요청을 보냅니다.

    // 사용자 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<UserDto> profile(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return ResponseEntity.ok(convertToDto(user));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // 아이디 찾기
    @PostMapping("/find-username")
    public ResponseEntity<String> findUsername(@RequestParam("email") String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            sendUsernameEmail(email, user.getUsername());
            return ResponseEntity.ok("Username has been sent to your email");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with this email");
        }
    }
    //Type: x-www-form-urlencoded
    //Key-Value:
    //email: testuser@example.com

    // 이메일로 사용자 이름 전송
    private void sendUsernameEmail(String to, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("아이디 찾기 결과");
        message.setText("회원님의 아이디는 " + username + "입니다.");
        mailSender.send(message);
    }

    // User를 UserDTO로 변환
    private UserDto convertToDto(User user) {
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
}

