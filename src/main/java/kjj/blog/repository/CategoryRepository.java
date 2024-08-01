package kjj.blog.repository;

import kjj.blog.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 카테고리 이름으로 카테고리 조회
    Category findByName(String name);
}
