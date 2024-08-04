package kjj.blog.service;

import kjj.blog.PostConverter;
import kjj.blog.domain.*;
import kjj.blog.repository.CategoryRepository;
import kjj.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final PostConverter postConverter;

    public List<PostDto> getPosts(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Slice<Post> posts = postRepository.findAllByOrderByIdDesc(pageable);
        log.info("Fetched posts page: {}", page);
        return posts.stream()
                .map(postConverter::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PostDto> getPostsByCategory(Long categoryId, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Slice<Post> posts = postRepository.findByCategoryOrderByPubDateDesc(category, pageable);
        log.info("Fetched posts for category: {} on page: {}", categoryId, page);
        return posts.stream()
                .map(postConverter::convertToDto)
                .collect(Collectors.toList());
    }

    public PostDto createPost(PostDto postDto, User user) {
        Category category = categoryRepository.findById(postDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setUser(user);
        post.setCategory(category);
        LocalDateTime now = LocalDateTime.now();
        post.setPubDate(now);
        post.setLastModified(now);

        Post savedPost = postRepository.save(post);
        log.info("Created post with ID: {}", savedPost.getId());
        return postConverter.convertToDto(savedPost);
    }

    public PostDto getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        log.info("Fetched post with ID: {}", id);
        return postConverter.convertToDto(post);
    }

    public PostDto updatePost(Long id, PostDto postDto, User user) {
        Post post = checkPost(id, user.getId());
        Category category = categoryRepository.findById(postDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setCategory(category);
        post.setLastModified(LocalDateTime.now());

        Post updatedPost = postRepository.save(post);
        log.info("Updated post with ID: {}", id);
        return postConverter.convertToDto(updatedPost);
    }

    public void deletePost(Long id, User user) {
        checkPost(id, user.getId());
        postRepository.deleteById(id);
        log.info("Deleted post with ID: {}", id);
    }

    private Post checkPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            log.error("User with ID: {} does not have permission to access post with ID: {}", userId, postId);
            throw new RuntimeException("Permission denied");
        }
        return post;
    }
}

