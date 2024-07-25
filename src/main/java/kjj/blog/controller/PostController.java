package kjj.blog.controller;

import jakarta.servlet.http.HttpSession;
import kjj.blog.domain.Post;
import kjj.blog.domain.User;
import kjj.blog.repository.PostRepository;
import kjj.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@Controller
@RequiredArgsConstructor
public class PostController {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String home(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10); // Show 10 posts per page
        Slice<Post> posts = postRepository.findAllByOrderByIdDesc(pageable);
        model.addAttribute("list", posts);
        model.addAttribute("page", page); // Add current page to model for pagination
        return "home";
    }


    @GetMapping("/post/list")
    public String list(@RequestParam(name = "page", defaultValue = "0") int page, HttpSession session, Model model) {
        session.setAttribute("page", page); // 현재 페이지를 세션에 저장
        Pageable pageable = PageRequest.of(page, 10);
        Slice<Post> posts = postRepository.findAllByOrderByIdDesc(pageable);
        model.addAttribute("list", posts);
        return "post/list"; // 반환할 뷰 이름
    }

    /** 글쓰기 화면 */
    @GetMapping("/post/create")
    public String create() {
        return "post/create";
    }

    @PostMapping("/post/create")
    public String create(@ModelAttribute Post post, @SessionAttribute("user") User user) {
        post.setUser(user);
        LocalDateTime now = LocalDateTime.now();
        post.setPubDate(now);
        post.setLastModified(now);
        postRepository.save(post);
        return "redirect:/post/list";
    }
    /** 글보기 */
    @GetMapping("/post/detail")
    public String detail(@RequestParam Long id, Model model) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        model.addAttribute("post", post);
        return "post/detail";
    }
    /** View User Details */
    @GetMapping("/user/detail")
    public String userDetail(@RequestParam Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "user/detail"; // Return the view name for user details
    }


    /** 글수정 화면 */
    @GetMapping("/post/update")
    public String updateForm(@RequestParam Long id, @SessionAttribute("user") User user, Model model) {
        Post post = checkPost(id, user.getId());
        model.addAttribute("post", post);
        return "post/update";
    }

    /** 글수정 */
    @PostMapping("/post/update")
    public String update(@ModelAttribute Post post, @SessionAttribute("user") User user) {
        checkPost(post.getId(), user.getId());
        postRepository.update(post);
        return "redirect:/post/detail?id=" + post.getId();
    }

    /** 글삭제 */
    @PostMapping("/post/delete")
    public String delete(@RequestParam Long id, @SessionAttribute("user") User user,
                         @SessionAttribute("page") int page) {
        checkPost(id, user.getId());
        postRepository.deleteById(id);
        return "redirect:/post/list?page=" + page;
    }

    /** 게시글의 권한 체크*/
    private Post checkPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("User not authorized to edit this post");
        }
        return post;
    }

}
