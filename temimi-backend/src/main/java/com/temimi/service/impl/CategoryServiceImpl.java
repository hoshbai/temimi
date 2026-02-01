package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.mapper.CategoryMapper;
import com.temimi.model.entity.Category;
import com.temimi.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Override
    public List<Category> getAllMainCategories() {
        // 查询所有分类，然后在Java层去重获取主分区
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("mc_id", "sc_id");
        List<Category> allCategories = categoryMapper.selectList(queryWrapper);

        // 使用 Stream API 按 mc_id 去重，保留第一个
        return allCategories.stream()
                .collect(Collectors.toMap(
                    Category::getMcId,
                    category -> category,
                    (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .sorted((c1, c2) -> c1.getMcId().compareTo(c2.getMcId()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Category> getSubCategoriesByMcId(String mcId) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mc_id", mcId)
                   .orderByAsc("sc_id");
        return categoryMapper.selectList(queryWrapper);
    }
    
    @Override
    public Category getCategoryByScId(String scId) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sc_id", scId);
        return categoryMapper.selectOne(queryWrapper);
    }
    
    @Override
    public List<String> getRecommendTags(String scId) {
        Category category = getCategoryByScId(scId);
        if (category == null || category.getRcmTag() == null) {
            return List.of();
        }

        return Arrays.stream(category.getRcmTag().split("\n"))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toList());
    }

    @Override
    public List<Category> getAllCategories() {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("mc_id", "sc_id");
        return categoryMapper.selectList(queryWrapper);
    }

    @Override
    public String getMcIdByScId(String scId) {
        Category category = getCategoryByScId(scId);
        return category != null ? category.getMcId() : null;
    }
}