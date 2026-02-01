package com.temimi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.temimi.model.entity.Category;
import java.util.List;

/**
 * 分区服务接口
 */
public interface CategoryService extends IService<Category> {
    
    /**
     * 获取所有主分区
     * @return 主分区列表
     */
    List<Category> getAllMainCategories();
    
    /**
     * 根据主分区ID获取子分区
     * @param mcId 主分区ID
     * @return 子分区列表
     */
    List<Category> getSubCategoriesByMcId(String mcId);
    
    /**
     * 根据子分区ID获取分区信息
     * @param scId 子分区ID
     * @return 分区信息
     */
    Category getCategoryByScId(String scId);
    
    /**
     * 获取分区的推荐标签
     * @param scId 子分区ID
     * @return 推荐标签列表
     */
    List<String> getRecommendTags(String scId);

    /**
     * 获取所有分类（包含所有数据）
     * @return 所有分类列表
     */
    List<Category> getAllCategories();

    /**
     * 根据子分区ID获取主分区ID
     * @param scId 子分区ID
     * @return 主分区ID，如果不存在返回null
     */
    String getMcIdByScId(String scId);
}