package kjj.blog.repository;

import kjj.blog.domain.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostRepository extends JpaRepository<Post,Long> {
    /**
     * Slice로 가져오는 목록. 전체 갯수가 없다.
     * Page는 전체 갯수도 가져옴
     */
    Slice<Post> findAllByOrderByIdDesc(Pageable pageable);

    /**
     * 글수정
     */
    @Modifying
    @Transactional
    @Query("update Post set title = :#{#post.title}, content = :#{#post.content} where id = :#{#post.id}")
    void update(Post post);

    Slice<Post> findByUserId(Long userId, Pageable pageable);
    List<Post> findAllByOrderByIdDesc();
}
