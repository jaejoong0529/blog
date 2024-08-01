package kjj.blog.controller;

import kjj.blog.domain.Category;
import kjj.blog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public String list(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "admin/categories/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/categories/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Category category) {
        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/update")
    public String updateForm(@RequestParam Long id, Model model) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        model.addAttribute("category", category);
        return "admin/categories/update";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Category category) {
        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id) {
        categoryRepository.deleteById(id);
        return "redirect:/admin/categories";
    }
}
