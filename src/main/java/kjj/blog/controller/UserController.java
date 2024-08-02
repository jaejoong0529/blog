package kjj.blog.controller;

import jakarta.servlet.http.HttpSession;
import kjj.blog.domain.User;
import kjj.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
//MVC방식 html HTML 페이지를 생성하고 반환하는 방식
//@Controller // View를 반환하기위해 사용
@RequiredArgsConstructor//final인 필드 값만 파라미터로 받는 생성자
//final: 초기화 후 값 변경 불가
//없을경우에 public UserController{UserRepository userRepository...
//this.userRepository=userRepository; ...}
public class UserController {

    private final UserRepository userRepository;//유저 리포지토리
    private final PasswordEncoder passwordEncoder;//비밀번호 암호화
    private final JavaMailSender mailSender;//아이디 찾기에서 메일 사용

    @GetMapping("/user/list")//데이터 조회, 뷰 반환 Restful 공부하기
    public String list(@RequestParam(name = "page", defaultValue = "0") int page, HttpSession session,Model model) {
        //@RequsetParam:HTTP 요청 파라미터("page")를 메서드 매개변수(int page)에 바인딩. simple type 일때
        //Model: 컨트롤러에서 뷰로 데이터를 전달하기 위해 사용
        session.setAttribute("page",page); //현재 페이지를 세션에 저장하여 유지
        Pageable pageable = PageRequest.of(page, 10);//페이지 요청을 생성합니다.
        Page<User> users = userRepository.findAll(pageable);  // 페이지네이션된 사용자 목록 조회
        //Page: 전체 페이지 수와 전체 항목 수를 제공하며, 페이지 내비게이션과 관련된 모든 정보를 포함합니다
        model.addAttribute("list", users);// 뷰에서 렌더링할 때 사용할 속성을 모델에 추가
        return "user/list";
    }
    //회원가입
    @GetMapping("/user/signin")
    public String signUpForm() {
        return "user/signin";
    }

    @PostMapping("/user/signin")//데이터 제출, 리소스 생성/수정
    public String signUp(@ModelAttribute("user") User user, HttpSession session,RedirectAttributes redirectAttributes) {
        //요청 파라미터를 특정 객체에 바인딩하고, 해당 객체를 모델에 추가하여 뷰에 전달 simple type아닐때
        //user객체에서 수정필요한 값만 set으로 표시
        boolean exists = userRepository.existsByUsername(user.getUsername());//중복확인
        if (!exists) {//중복아닐때
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setDateJoined(LocalDateTime.now());
            user.setLastLogin(user.getDateJoined());
            userRepository.save(user);//저장
            session.setAttribute("user", user);
            return "redirect:/";
        } else {  // 아이디 존재. 등록 실패
//            return "redirect:/user/signin?error";
            redirectAttributes.addFlashAttribute("error", "아이디가 존재합니다.");
            //error라는 이름의 플래시 속성을 설정하여 오류 메시지를 user/signin 페이지로 전달
            // 메시지 바꿀려면 컨트롤러랑 html 둘다 수정
            return "redirect:/user/signin";
        }
    }

    // 로그인
    @GetMapping("/user/login")
    public String loginForm() {
        return "user/login";
    }

    @PostMapping("/user/login")
    public String login(@RequestParam("username") String username,//HTTP 요청에서 username 파라미터 값을 추출하여 username 변수에 저장 사용자가 입력한 아이디
                        @RequestParam("password") String password,//비밀번호
                        HttpSession session,RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(username);//데이터베이스에서 사용자가 입력한 username과 일치하는 사용자 정보를 검색
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            //사용자가 데이터베이스에 존재하는지 확인,
            // 사용자가 입력한 비밀번호와 데이터베이스에 저장된 비밀번호(user.getPassword())가 일치하는지 확인
            session.setAttribute("user", user);
            user.setLastLogin(LocalDateTime.now());
            userRepository.updateLastLogin(user.getId(), user.getLastLogin());//마지막 로그인 시간을 데이터베이스에 업데이트
            return "redirect:/";
        } else {
            // 로그인 실패
//            return "redirect:/user/login?error";
            redirectAttributes.addFlashAttribute("error", "아이디나 비밀번호가 틀렸습니다.");
            return "redirect:/user/login";
        }
    }

    //로그아웃

    @PostMapping("/user/logout")
    public String logout(HttpSession session) {
        session.invalidate();// 세션을 무효화
        return "redirect:/";
    }

    //해지
    @PostMapping("/user/delete")
    public String delete(@SessionAttribute("user") User user) {
        //@SessionAttribute는 주로 세션에서 값을 읽는 데 사용되며, 값을 설정할 때는 HttpSession사용
        try {
            userRepository.deleteById(user.getId());
            return "forward:/user/logout";//forward를 사용하면 브라우저 주소창의 URL은 변경되지 않는다.
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
    //@GetMapping("/user/profile")
    public String profile(@SessionAttribute("user") User user, Model model) {
        if (user != null) {
            model.addAttribute("user", user);
            return "user/profile"; // 사용자 프로필 페이지로 이동
        } else {
            return "redirect:/user/login"; // 로그인 페이지로 리다이렉트
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






