package org.michael.product.service;

import org.michael.product.dto.CategorySaveDTO;
import org.michael.product.vo.CategoryTreeVO;

import java.util.List;

public interface CategoryService {

    Long addCategory(CategorySaveDTO dto);

    void updateCategory(Long id, CategorySaveDTO dto);

    void deleteCategory(Long id);

    List<CategoryTreeVO> getCategoryTree();
}
