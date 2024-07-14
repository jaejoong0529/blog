package kjj.blog.controller;

import jakarta.servlet.http.HttpSession;
import kjj.blog.domain.User;
import kjj.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class UserController {

    private  UserRepository userRepository;

    @GetMapping("/user/list")
    public String list( ) {
        return "user/list";
    }

    @GetMapping("/user/signin")
    public String signUpForm() {
        // 회원 가입 폼을 보여주는 로직
        return "/user/signin";
    }
    //회원가입
    @PostMapping("/user/signin")
    public String signUp(@ModelAttribute("user") User user, HttpSession session) {
        boolean exists= userRepository.existsByUsername(user.getUsername());
        if (!exists) {
           // user.setPassword();
            user.setDateJoined(LocalDateTime.now());
            user.setLastLogin(user.getDateJoined());
            userRepository.save(user);
            session.setAttribute("user", user);
            return "redirect:/";
        }
        else {  // 이메일 존재. 등록 실패
            return "redirect:/user/signup?error";
        }
    }
    @GetMapping("/user/login")
    public String loginForm() {
        // 회원 가입 폼을 보여주는 로직
        return "/user/login";
    }
    //로그인
    @PostMapping("/user/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session) {
        User user = userRepository.findByUsername(username);
        if (user != null && password.equals(user.getPassword())) {
            // 비밀번호 매치
            session.setAttribute("user", user);
            user.setLastLogin(LocalDateTime.now());
            userRepository.updateLastLogin(user.getId(), user.getLastLogin());
            return "redirect:/";
        } else {  // 사용자가 없거나 비밀번호가 매치하지 않을 경우
            return "redirect:/user/login?error";
        }

    }



}
