package org.michael.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.result.Result;
import org.michael.product.dto.CategorySaveDTO;
import org.michael.product.service.CategoryService;
import org.michael.product.vo.CategoryCreateVO;
import org.michael.product.vo.CategoryTreeVO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/add")
    public Result<CategoryCreateVO> addCategory(@Valid @RequestBody CategorySaveDTO dto) {
        log.info("POST /categories/add dto={}, 时间:{}", dto, LocalDateTime.now());
        Long categoryId = categoryService.addCategory(dto);
        return Result.ok(new CategoryCreateVO(categoryId));
    }

    @PutMapping("/update/{id}")
    public Result<Boolean> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategorySaveDTO dto
    ) {
        log.info("PUT /categories/update/{id} id={}, dto={}, 时间:{}", id, dto, LocalDateTime.now());
        categoryService.updateCategory(id, dto);
        return Result.ok(true);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteCategory(@PathVariable Long id) {
        log.info("DELETE /categories/delete/{id} id={}, 时间:{}", id, LocalDateTime.now());
        categoryService.deleteCategory(id);
        return Result.ok(true);
    }

    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> getCategoryTree() {
        log.info("GET /categories/tree 时间:{}", LocalDateTime.now());
        List<CategoryTreeVO> tree = categoryService.getCategoryTree();
        return Result.ok(tree);
    }
}
