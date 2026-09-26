package org.michael.product.service;

import org.michael.product.dto.CategorySaveDTO;

public interface CategoryService {

    Long addCategory(CategorySaveDTO dto);

    void updateCategory(Long id, CategorySaveDTO dto);

    void deleteCategory(Long id);
}
