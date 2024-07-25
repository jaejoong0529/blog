package kjj.blog.controller;

import jakarta.servlet.http.HttpSession;
import kjj.blog.domain.User;
import kjj.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @GetMapping("/user/list")
    public String list() {
        return "user/list";
    }

    @GetMapping("/user/signin")
    public String signUpForm() {
        return "user/signin";
    }

    //회원가입
    @PostMapping("/user/signin")
    public String signUp(@ModelAttribute("user") User user, HttpSession session) {
        boolean exists = userRepository.existsByUsername(user.getUsername());
        if (!exists) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setDateJoined(LocalDateTime.now());
            user.setLastLogin(user.getDateJoined());
            userRepository.save(user);
            session.setAttribute("user", user);
            return "redirect:/";
        } else {  // 이메일 존재. 등록 실패
            return "redirect:/user/signup?error";
        }
    }

    @GetMapping("/user/login")
    public String loginForm() {
        return "user/login";
    }

    // 로그인
    @PostMapping("/user/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session) {
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            session.setAttribute("user", user);
            user.setLastLogin(LocalDateTime.now());
            userRepository.updateLastLogin(user.getId(), user.getLastLogin());
            return "redirect:/";
        } else {
            // 로그인 실패
            return "redirect:/user/login?error";
        }
    }

    //로그아웃

    @PostMapping("/user/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    //해지
    @PostMapping("/user/delete")
    public String delete(@SessionAttribute("user") User user) {
        try {
            userRepository.deleteById(user.getId());
            return "forward:/user/logout";
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                    "등록한 글들이 있어서 해지할 수 없습니다.\n글들을 먼저 삭제하세요.");
        }
    }
    @GetMapping("/user/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
            return "user/profile";
        } else {
            return "redirect:/user/login";
        }
    }
    @GetMapping("/user/find-username")
    public String findUsernameForm() {
        return "user/find-username";
    }

    // 아이디 찾기 처리
    @PostMapping("/user/find-username")
    public String findUsername(@RequestParam("email") String email, Model model) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            // 이메일로 사용자 이름을 전송하는 로직
            sendUsernameEmail(email, user.getUsername());
            return "redirect:/user/find-username?success";
        } else {
            return "redirect:/user/find-username?error";
        }
    }

    // 이메일로 사용자 이름 전송
    private void sendUsernameEmail(String to, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("아이디 찾기 결과");
        message.setText("회원님의 아이디는 " + username + "입니다.");
        mailSender.send(message);
    }

}






