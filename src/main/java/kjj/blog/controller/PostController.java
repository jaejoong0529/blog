package kjj.blog.controller;

import jakarta.servlet.http.HttpSession;
import kjj.blog.domain.Category;
import kjj.blog.domain.Post;
import kjj.blog.domain.User;
import kjj.blog.repository.CategoryRepository;
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
import java.util.List;


@Controller
@RequiredArgsConstructor
public class PostController {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping("/")
    public String home(@RequestParam(name = "page", defaultValue = "0") int page, HttpSession session,Model model) {
        session.setAttribute("page", page);
        Pageable pageable = PageRequest.of(page, 10);
        Slice<Post> posts = postRepository.findAllByOrderByIdDesc(pageable);//페이지 사이즈 10으로 해서 slice사용
        //list는 모든 항목 조회, 페이지네이션 기능이 없다.  slice는 부분 항목 조회, 다음페이지 존재 여부만 알수있음
        // 전체 카테고리 목록 가져오기
        List<Category> categories = categoryRepository.findAll();

        model.addAttribute("list", posts);
        model.addAttribute("categories", categories); // 카테고리 목록 모델에 추가
        model.addAttribute("page", page);

        return "home";
    }

    @GetMapping("/category/{id}")//{id} 변수에 해당하는 값을 메서드의 매개변수로 전달받는다.
    public String category(@PathVariable Long id, @RequestParam(name = "page", defaultValue = "0") int page, HttpSession session,Model model) {
        session.setAttribute("page",page);
        //@PathVariable 어노테이션을 사용하여 URL 경로에서 전달된 카테고리 ID 값을 id 매개변수로 받는다.
        Pageable pageable = PageRequest.of(page, 10); // 한 페이지에 10개의 게시글
        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        Slice<Post> posts = postRepository.findByCategoryOrderByPubDateDesc(category, pageable);

        model.addAttribute("list", posts);
        model.addAttribute("category", category);
        model.addAttribute("page", page);
        return "category/detail";
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
    public String create(Model model) {
        List<Category> categories = categoryRepository.findAll(); // 카테고리 목록 가져오기
        model.addAttribute("categories", categories); // 모델에 카테고리 추가
        return "post/create";
    }

    /** 글쓰기 */
    @PostMapping("/post/create")
    public String create(@ModelAttribute Post post,//Post 객체로 변환하여 post 매개변수에 바인딩
                         @RequestParam Long categoryId, // 카테고리 ID 추가
                         @SessionAttribute("user") User user) {//현재 로그인한 사용자 정보 가져옴
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다.")); // 카테고리 조회

        post.setUser(user);
        post.setCategory(category); // 게시글에 카테고리 설정
        LocalDateTime now = LocalDateTime.now();
        post.setPubDate(now);
        post.setLastModified(now);
        postRepository.save(post);
        return "redirect:/";
    }
    /** 글보기 */
    @GetMapping("/post/detail")
    public String detail(@RequestParam Long id, Model model) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        model.addAttribute("post", post);
        return "post/detail";
    }
    /** 유저 정보 보기 */
    @GetMapping("/user/detail")
    public String userDetail(@RequestParam Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "user/detail";
    }

    /** 글수정 화면 */
    @GetMapping("/post/update")
    public String updateForm(@RequestParam Long id, @SessionAttribute("user") User user, Model model) {
        Post post = checkPost(id, user.getId());//게시글 권한 확인 및 조회
        List<Category> categories = categoryRepository.findAll(); // 카테고리 목록 가져오기
        model.addAttribute("post", post);
        model.addAttribute("categories", categories); // 모델에 카테고리 추가
        return "post/update";
    }

    /** 글수정 */
    @PostMapping("/post/update")
    public String update(@ModelAttribute Post post, @RequestParam Long categoryId, @SessionAttribute("user") User user) {
        checkPost(post.getId(), user.getId());// 게시글 권한 확인
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        // 게시글 업데이트
        postRepository.updatePost(post.getId(), post.getTitle(), post.getContent(), category);
        return "redirect:/post/detail?id=" + post.getId();
    }

    /** 글삭제 */
    @PostMapping("/post/delete")
    public String delete(@RequestParam Long id, @SessionAttribute("user") User user) {
        checkPost(id, user.getId());
        postRepository.deleteById(id);
        return "redirect:/";
    }
    /** 게시글의 권한 체크*/
    private Post checkPost(@RequestParam Long postId, @RequestParam Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("게시물이 없습니다"));
        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다");
        }
        return post;
    }
}