package kjj.blog.controller;

import jakarta.servlet.http.HttpSession;
import kjj.blog.domain.PostDto;
import kjj.blog.domain.User;
import kjj.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor

@Slf4j
public class PostControllerV3 {
    private final PostService postService;
    @GetMapping("/")
    public ResponseEntity<List<PostDto>> getPosts(@RequestParam(name = "page", defaultValue = "0") int page,
                                                  HttpSession session) {
        session.setAttribute("page", page);
        List<PostDto> posts = postService.getPosts(page);
        log.info("Fetched posts for page: {}", page);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<List<PostDto>> category(@PathVariable(name = "id") Long id,
                                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                                  HttpSession session) {
        session.setAttribute("page", page);
        List<PostDto> posts = postService.getPostsByCategory(id, page);
        log.info("Fetched posts for category ID: {} on page: {}", id, page);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/list")
    public ResponseEntity<List<PostDto>> list(@RequestParam(name = "page", defaultValue = "0") int page,
                                              HttpSession session) {
        session.setAttribute("page", page);
        List<PostDto> posts = postService.getPosts(page);
        log.info("Fetched posts for page: {}", page);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/create")
    public ResponseEntity<PostDto> create(@RequestBody PostDto postDto,
                                          @SessionAttribute("user") User user) {
        PostDto createdPost = postService.createPost(postDto, user);
        log.info("Created post with ID: {}", createdPost.getId());
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> postDetail(@PathVariable(name = "id") Long id) {
        PostDto post = postService.getPost(id);
        log.info("Fetched post with ID: {}", id);
        return ResponseEntity.ok(post);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable(name = "id") Long id,
                                              @RequestBody PostDto postDto,
                                              @SessionAttribute("user") User user) {
        PostDto updatedPost = postService.updatePost(id, postDto, user);
        log.info("Updated post with ID: {}", id);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable(name = "id") Long id,
                                           @SessionAttribute("user") User user) {
        postService.deletePost(id, user);
        log.info("Deleted post with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}


