package kjj.blog.service;


import jakarta.transaction.Transactional;
import kjj.blog.UserConverter;
import kjj.blog.domain.User;
import kjj.blog.domain.UserDto;
import kjj.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional//서비스에서는 Transactional사용
@Slf4j // Slf4j 어노테이션 추가  로그사용
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    // 사용자 이름으로 사용자가 존재하는지 확인하는 메서드
    public boolean userExistsByUsername(String username) {
        log.info("Checking if user exists by username: {}", username); // 로그 추가
        return userRepository.existsByUsername(username);
    }

//    public void registerUser(UserDto userDto) { ->UserConverter사용으로 축약
//        User user = new User();
//        user.setUsername(userDto.getUsername());
//        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
//        user.setNickname(userDto.getNickname());
//        user.setEmail(userDto.getEmail());
//        user.setPhoneNumber(userDto.getPhoneNumber());
//        user.setDateJoined(LocalDateTime.now());
//        user.setLastLogin(user.getDateJoined());
//        userRepository.save(user);
//    }
// 사용자 등록을 처리하는 메서드
    public void registerUser(UserDto userDto) {
        log.info("Registering user: {}", userDto.getUsername()); // 로그 추가
        User user = UserConverter.fromDto(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setDateJoined(LocalDateTime.now());
        user.setLastLogin(user.getDateJoined());
        userRepository.save(user);
        log.info("User registered successfully: {}", userDto.getUsername()); // 로그 추가
    }

    // 사용자 이름으로 사용자를 찾는 메서드
    public Optional<User> findUserByUsername(String username) {
        log.info("Finding user by username: {}", username); // 로그 추가
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    // 사용자의 마지막 로그인 시간을 업데이트하는 메서드
    public void updateUserLastLogin(Long userId, LocalDateTime lastLogin) {
        log.info("Updating last login for user ID: {}", userId); // 로그 추가
        userRepository.updateLastLogin(userId, lastLogin);
    }
    // 사용자 이름을 이메일로 보내는 메서드
    public void sendUsernameEmail(String to, String username) {
        log.info("Sending username email to: {}", to); // 로그 추가
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("아이디 찾기 결과");
        message.setText("회원님의 아이디는 " + username + "입니다.");
        mailSender.send(message);
        log.info("Username email sent to: {}", to); // 로그 추가
    }

    // 사용자를 삭제하는 메서드
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id); // 로그 추가
        userRepository.deleteById(id);
        log.info("User deleted with ID: {}", id); // 로그 추가
    }

    // 이메일로 사용자를 찾는 메서드
    public Optional<User> findUserByEmail(String email) {
        log.info("Finding user by email: {}", email); // 로그 추가
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    // 비밀번호를 비교하는 메서드
    public boolean passwordMatches(String rawPassword, String encodedPassword) {
        log.debug("Checking password match"); // 디버그 로그 추가
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // 모든 사용자를 페이징하여 찾는 메서드
    public Page<User> findAllUsers(Pageable pageable) {
        log.info("Finding all users with pagination"); // 로그 추가
        return userRepository.findAll(pageable);
    }
}

