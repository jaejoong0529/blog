package kjj.blog;

import kjj.blog.domain.Post;
import kjj.blog.domain.PostDto;
import org.springframework.stereotype.Component;

@Component
public class PostConverter {

    public PostDto convertToDto(Post post) {
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
    //post에는 fromdto가 없는 이유
    //주로 읽기 작업에 사용: 보통 PostConverter는 엔티티를 DTO로 변환하는 toDto 메서드가 필요합니다.
    // 이 메서드는 서버에서 클라이언트로 데이터를 보내기 위해 사용됩니다.
    //생성 및 수정은 별도의 로직: 새 게시글을 만들거나 수정할 때는 PostDto를 직접 엔티티로 변환하기보다는,
    // DTO에서 필요한 데이터만 추출해서 엔티티의 필드를 직접 수정합니다.

    //PostConverter에서 fromDto 메서드를 추가할 필요가 없던 이유는 엔티티를 새로 생성하는 것이 아니라,
    // 이미 존재하는 엔티티를 수정하기 때문입니다.
}
