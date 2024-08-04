package kjj.blog.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostDto {
        private Long id;
        private String title;
        private String content;
        private LocalDateTime pubDate;
        private LocalDateTime lastModified;
        private Long categoryId;
        private String categoryName;
        private Long userId;
        private String username;
}
