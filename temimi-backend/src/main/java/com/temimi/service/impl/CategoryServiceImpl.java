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

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Override
    public List<Category> getAllMainCategories() {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT mc_id", "mc_name")
                   .orderByAsc("mc_id");
        return categoryMapper.selectList(queryWrapper);
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
}