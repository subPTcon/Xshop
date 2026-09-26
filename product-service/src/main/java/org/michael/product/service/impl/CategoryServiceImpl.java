package org.michael.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.michael.common.exception.BusinessException;
import org.michael.common.exception.ErrorCode;
import org.michael.product.dto.CategorySaveDTO;
import org.michael.product.mapper.CategoryMapper;
import org.michael.product.mapper.ProductMapper;
import org.michael.product.pojo.Category;
import org.michael.product.pojo.Product;
import org.michael.product.service.CategoryService;
import org.michael.product.vo.CategoryTreeVO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    @Override
    public Long addCategory(CategorySaveDTO dto) {
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

    @Override
    public void updateCategory(Long id, CategorySaveDTO dto) {
        Category category = categoryMapper.selectById(id);

        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        if (id.equals(dto.getParentId())) {
            throw new BusinessException(ErrorCode.CATEGORY_PARENT_INVALID);
        }

        if (dto.getParentId() != 0L) {
            Category parentCategory = categoryMapper.selectById(dto.getParentId());

            if (parentCategory == null) {
                throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
            }

            if (isDescendant(id, dto.getParentId())) {
                throw new BusinessException(ErrorCode.CATEGORY_PARENT_INVALID);
            }
        }

        category.setParentId(dto.getParentId());
        category.setName(dto.getName());
        category.setSortOrder(dto.getSortOrder());

        categoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        // 1.类目是否存在
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // 2.是否存在子类目
        Long childCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getParentId, id)
        );
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_CHILDREN);
        }

        // 3.是否存在商品引用该类目
        Long productCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getCategoryId, id)
        );

        if (productCount > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_PRODUCTS);
        }

        categoryMapper.deleteById(id);
    }

    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        // 1.一次性查询全部类目
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSortOrder)
                        .orderByAsc(Category::getId)
        );
        if (categories.isEmpty()) {
            return Collections.emptyList();
        }

        // 2.按 parentId 分组
        Map<Long, List<Category>> categoryMap = categories.stream().collect(Collectors.groupingBy(Category::getParentId));

        return buildTree(0L, categoryMap);
    }

    private List<CategoryTreeVO> buildTree(Long parentId, Map<Long, List<Category>> categoryMap) {
        List<Category> children = categoryMap.get(parentId);
        if (children == null || children.isEmpty()) {
            return Collections.emptyList();
        }

        return children.stream()
                .map(category -> {
                    CategoryTreeVO vo = new CategoryTreeVO();

                    vo.setId(category.getId());
                    vo.setName(category.getName());

                    vo.setChildren(buildTree(category.getId(), categoryMap));

                    return vo;
                }).toList();
    }

    private boolean isDescendant(Long currentId, Long targetParentId) {
        Long parentId = targetParentId;

        while (parentId != 0L) {
            if (currentId.equals(parentId)) {
                return true;
            }

            Category category = categoryMapper.selectById(parentId);

            if (category == null) {
                return false;
            }

            parentId = category.getParentId();
        }

        return false;
    }
}
