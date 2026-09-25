package org.michael.product.service.impl;

import lombok.RequiredArgsConstructor;
import org.michael.common.exception.BusinessException;
import org.michael.common.exception.ErrorCode;
import org.michael.product.dto.CategoryCreateDTO;
import org.michael.product.mapper.CategoryMapper;
import org.michael.product.pojo.Category;
import org.michael.product.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public Long addCategory(CategoryCreateDTO dto) {
        // parentId = 0 表示创建一级类目
        if (dto.getParentId() != 0L) {
            Category parentCategory = categoryMapper.selectById(dto.getParentId());

            if (parentCategory == null) {
                throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
            }
        }

        Category category = new Category();
        category.setParentId(dto.getParentId());
        category.setName(dto.getName());
        category.setSortOrder(dto.getSortOrder());

        categoryMapper.insert(category);
        return category.getId();
    }
}
