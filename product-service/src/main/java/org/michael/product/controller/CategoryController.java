package org.michael.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.result.Result;
import org.michael.product.dto.CategoryCreateDTO;
import org.michael.product.service.CategoryService;
import org.michael.product.vo.CategoryCreateVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/add")
    public Result<CategoryCreateVO> addCategory(@Valid @RequestBody CategoryCreateDTO dto) {
        log.info("POST /categories/add dto={}, 时间:{}", dto, LocalDateTime.now());
        Long categoryId = categoryService.addCategory(dto);
        return Result.ok(new CategoryCreateVO(categoryId));
    }
}
