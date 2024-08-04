package kjj.blog.controller;

import jakarta.servlet.http.HttpSession;
import kjj.blog.domain.*;
import kjj.blog.repository.CategoryRepository;
import kjj.blog.repository.PostRepository;
import kjj.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")//전체적으로 주소를 /api/posts 시작
public class PostControllerV2 {
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 글목록
     */
    @GetMapping("/")
    public ResponseEntity<List<PostDto>> getPosts(@RequestParam(name = "page", defaultValue = "0") int page,
                                                  HttpSession session) {
        session.setAttribute("page", page);
        Pageable pageable = PageRequest.of(page, 10);
        Slice<Post> posts = postRepository.findAllByOrderByIdDesc(pageable);
        List<PostDto> postDtos = posts.stream()
                .map(this::convertToDto) // Post 엔티티를 PostDto로 변환
                .collect(Collectors.toList());
        //model.addAttribute대신 사용
        return ResponseEntity.ok(postDtos);
    }


    /**
     * 카테고리별 글목록
     */
    @GetMapping("/category/{id}")
    public ResponseEntity<List<PostDto>> category(@PathVariable(name = "id") Long id, @RequestParam(name = "page", defaultValue = "0") int page,
                                                  HttpSession session) {

        session.setAttribute("page", page);
        Pageable pageable = PageRequest.of(page, 10); // 한 페이지에 10개의 게시글
        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        Slice<Post> posts = postRepository.findByCategoryOrderByPubDateDesc(category, pageable);
        List<PostDto> postDtos = posts.stream()
                .map(this::convertToDto) // Post 엔티티를 PostDto로 변환
                .collect(Collectors.toList());
        return ResponseEntity.ok(postDtos);
    }

    /**
     * 글목록
     */
    @GetMapping("/list")
    public ResponseEntity<List<PostDto>> list(@RequestParam(name = "page", defaultValue = "0") int page,
                                              HttpSession session) {
        session.setAttribute("page", page); // 현재 페이지를 세션에 저장
        Pageable pageable = PageRequest.of(page, 10);
        Slice<Post> posts = postRepository.findAllByOrderByIdDesc(pageable);
        List<PostDto> postDtos = posts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(postDtos);
    }

    /**
     * 글쓰기 화면
     */
    @PostMapping("/create")
    public ResponseEntity<PostDto> create(@RequestBody PostDto postDto,//@ModelAttribute Post post대신사용
                                          @SessionAttribute("user") User user) {
        Category category = categoryRepository.findById(postDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다.")); // 카테고리 조회
        Post post = new Post();
        post.setTitle(postDto.getTitle());  // 제목 설정
        post.setContent(postDto.getContent());  // 내용 설정
        post.setUser(user);  // 사용자 설정
        post.setCategory(category);  // 카테고리 설정
        LocalDateTime now = LocalDateTime.now();
        post.setPubDate(now);  // 게시일 설정
        post.setLastModified(now);  // 마지막 수정일 설정

        // 데이터베이스에 Post 저장
        Post savedPost = postRepository.save(post);

        // 저장된 Post를 PostDto로 변환
        PostDto responseDto = convertToDto(savedPost);

        // 변환된 PostDto를 ResponseEntity로 반환
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }//body에 raw에서 로그인 먼저한 후
    //{
    //    "title": "New Post Title",
    //    "content": "New Post Content",
    //    "categoryId": 1
    //}

    /** 글 보기 */
    @GetMapping("/{id}")
    public ResponseEntity<PostDto> postDetail(@PathVariable (name = "id") Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Post를 PostDto로 변환
        PostDto postDto = convertToDto(post);

        // PostDto를 ResponseEntity로 반환
        return ResponseEntity.ok(postDto);
    }
    /**
     * 게시글 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable  (name = "id") Long id,
            @RequestBody PostDto postDto,
            @SessionAttribute("user") User user) {

        Post post = checkPost(id, user.getId());

        Category category = categoryRepository.findById(postDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setCategory(category);
        post.setLastModified(LocalDateTime.now());

        Post updatedPost = postRepository.save(post);

        return ResponseEntity.ok(convertToDto(updatedPost));
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            //ResponseEntity<Void>는 HTTP 응답의 바디 부분이 없음을 명시적으로 나타냅니다.
            // 즉, 클라이언트에게 데이터를 반환할 필요가 없는 경우에 사용됩니다.
            @PathVariable  (name = "id") Long id,
            @SessionAttribute("user") User user) {

        checkPost(id, user.getId());
        postRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * 게시글의 권한 체크
     */
    private Post checkPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시물이 없습니다"));

        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다");
        }
        return post;
    }

    private PostDto convertToDto(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setPubDate(post.getPubDate());
        dto.setLastModified(post.getLastModified());
        dto.setCategoryId(post.getCategory().getId());
        dto.setCategoryName(post.getCategory().getName());
        dto.setUserId(post.getUser().getId());
        dto.setUsername(post.getUser().getUsername());
        return dto;
    }

}
