package com.temimi.controller.category;

import com.temimi.model.entity.Category;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有主分区
     * GET /api/category/main
     */
    @GetMapping("/main")
    public ApiResult<List<Category>> getMainCategories() {
        try {
            List<Category> categories = categoryService.getAllMainCategories();
            return ApiResult.success(categories);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 根据主分区ID获取子分区
     * GET /api/category/sub/{mcId}
     */
    @GetMapping("/sub/{mcId}")
    public ApiResult<List<Category>> getSubCategories(@PathVariable String mcId) {
        try {
            List<Category> categories = categoryService.getSubCategoriesByMcId(mcId);
            return ApiResult.success(categories);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 根据子分区ID获取分区信息
     * GET /api/category/info/{scId}
     */
    @GetMapping("/info/{scId}")
    public ApiResult<Category> getCategoryInfo(@PathVariable String scId) {
        try {
            Category category = categoryService.getCategoryByScId(scId);
            return ApiResult.success(category);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取分区推荐标签
     * GET /api/category/tags/{scId}
     */
    @GetMapping("/tags/{scId}")
    public ApiResult<List<String>> getRecommendTags(@PathVariable String scId) {
        try {
            List<String> tags = categoryService.getRecommendTags(scId);
            return ApiResult.success(tags);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}