package kjj.blog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Entity
@Getter@Setter
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    private LocalDateTime pubDate;
    private LocalDateTime lastModified;

    // 디폴트 생성자
    public Post() {}

    public Post(Long id, String title, String content, User user,Category category, LocalDateTime pubDate, LocalDateTime lastModified) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.user = user;
        this.category = category;
        this.pubDate = pubDate;
        this.lastModified = lastModified;
    }
    public String getPubDateFormatted() {
        return formatted(pubDate);
    }

    public String getLastModifiedFormatted() {
        return formatted(lastModified);
    }

    private String formatted(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}
