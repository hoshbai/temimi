package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏夹 Mapper 接口
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
    // 基础CRUD已由BaseMapper提供
}