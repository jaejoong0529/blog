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

//@RestController//RestApi를 위해 Controller대신 RestController사용
@RequestMapping("/api/users")//전체적으로 주소를 /api/users로 시작
@RequiredArgsConstructor// final인 필드값만 파라미터로 받는 생성자
public class UserControllerV2 {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    // 유저 목록 조회
    @GetMapping
    public ResponseEntity<List<UserDto>> list(@RequestParam(name = "page", defaultValue = "0") int page,HttpSession session) {
        //ResponseEntity<List<UserDto>>: 메서드가 반환하는 타입
        //ResponseEntity는 HTTP 응답을 감싸는 클래스로 List<UserDto>가 응답 본문으로 포함
        session.setAttribute("page",page);
        Pageable pageable = PageRequest.of(page, 10);
        Page<User> users = userRepository.findAll(pageable);
        List<UserDto> userDTOs = users.getContent().stream()
                //users.getContent(): Page 객체에서 현재 페이지의 콘텐츠를 가져옵니다. 이 콘텐츠는 List<User> 타입
                //여기서 List는 순서가 있는 요소의 잡합을 나타내는 컬렉션이다.
                //getContent() 메서드는 현재 페이지에 해당하는 User 객체들의 리스트(List<User>)를 반환
                //.stream(): List<User>를 스트림으로 변환하여 다양한 스트림 연산을 수행할 수 있게 합니다.
                .map(this::convertToDto)
                //User 객체를 UserDto 객체로 변환합니다. convertToDto는 사용자 엔티티를 DTO로 변환하는 메서드
                .toList();  //collect(Collectors.toList());
        //.collect(Collectors.toList()): 변환된 DTO들을 List<UserDto>로 수집합니다.
        return ResponseEntity.ok(userDTOs);
        //ResponseEntity.ok(userDTOs): 변환된 사용자 DTO 리스트를 포함한 HTTP 200 OK 응답을 생성하여 반환
    }
    //즉 컬렉션을 스트림으로 변환후 스트림을 통해 데이터 처리작업을 수행하고 다시 컬렉션으로 수집하는 과정


    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody UserDto userDto) {
        //<String> 주로 상태 메시지나 간단한 정보를 전달
        //매개변수: @RequestBody 어노테이션을 통해 클라이언트가 전송한 JSON 형식의 요청 본문을 UserDto 객체로 변환하여 매개변수로 받습니다.
        //반환 타입: ResponseEntity<String>는 HTTP 응답을 반환하며, 응답 본문으로 문자열을 포함하고 HTTP 상태 코드를 설정할 수 있습니다.
        boolean exists = userRepository.existsByUsername(userDto.getUsername());
        if (!exists) {
            User user = new User();//새 User 객체를 생성
            user.setUsername(userDto.getUsername());//UserDto에서 사용자 이름을 가져와 User 객체에 설정
            user.setPassword(passwordEncoder.encode(userDto.getPassword())); // 비밀번호 암호화
            user.setNickname(userDto.getNickname());
            user.setEmail(userDto.getEmail());
            user.setPhoneNumber(userDto.getPhoneNumber());
            user.setDateJoined(LocalDateTime.now());
            user.setLastLogin(user.getDateJoined());
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } else {//회원가입이 성공하면 HTTP 상태 코드를 201 (Created)로 설정하고, 응답 본문에 성공 메시지를 담아 반환합니다.
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }//HTTP 상태 코드를 409 (Conflict)로 설정하고, 응답 본문에 사용자 이름이 이미 존재한다는 메시지를 담아 반환합니다.
    }
    //postman에서 테스트 raw json, body에{
    //    "username": "testuser",
    //    "password": "password123!",
    //    "nickname": "Test User",
    //    "email": "testuser@example.com",
    //    "phoneNumber": "123-456-7890"
    //}
    //

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
        }// HTTP 상태 코드 401 (Unauthorized)와 함께 로그인 실패 메시지를 응답 본문에 담아 반환합니다.
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
        }//400오류 클라이언트의 요청이 잘못되었습니다.
    }
    //Method: DELETE
    //
    //URL: http://localhost:8080/{id} (여기서 {id}는 실제 사용자 ID로 대체해야 합니다.)
    //
    //Body: 빈 내용으로 DELETE 요청을 보냅니다.

    // 사용자 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<UserDto> profile(HttpSession session) {
        //사용자의 프로필 정보를 JSON 형태로 클라이언트에 반환하고자 할 때 사용하기 때문에 <UserDto>
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return ResponseEntity.ok(convertToDto(user));
        } else {//convertToDto(user): User 객체를 UserDto로 변환하는 메서드입니다.
            //UserDto는 클라이언트에게 반환할 사용자 정보를 담고 있는 데이터 전송 객체
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }//HTTP 상태 코드 401을 설정합니다. 이 상태 코드는 인증되지 않은 요청을 나타냅니다.
        //.build(): 응답 본문을 설정하지 않고 빈 응답을 반환
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
        }//HTTP 상태 코드 404 (Not Found)
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

