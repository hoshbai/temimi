package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分区表 Mapper 接口
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    // 继承 BaseMapper 即可获得所有基础CRUD方法
    // 如需自定义复杂查询，可在此添加 @Select 等注解的方法
}