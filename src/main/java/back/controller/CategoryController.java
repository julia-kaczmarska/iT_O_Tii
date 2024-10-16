package back.controller;

import back.controller.dto.CategoryDTO;
import back.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // Pozwala na żądania z Twojej aplikacji React
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/user/{userId}/cats")
    public ResponseEntity<Object> getCategoriesByUserId(@PathVariable Long userId) {
        try {
            List<CategoryDTO> categories = categoryService.getCategoriesByUserId(userId);
            return ResponseEntity.ok(categories);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/user/{userId}/addcat")
    public ResponseEntity<Object> addCategory(@RequestBody CategoryDTO categoryDto, @PathVariable Long userId) {
        try {
            CategoryDTO savedCategory = categoryService.addCategory(categoryDto, userId);
            return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @PutMapping("/user/{userId}/cats/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long userId,
            @PathVariable Long categoryId,
            @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO updatedCategory = categoryService.updateCategory(userId, categoryId, categoryDTO);
        return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
    }

    @DeleteMapping("/user/{userId}/cats/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long userId,
            @PathVariable Long categoryId) {
        categoryService.deleteCategory(userId, categoryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
