package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.FavoriteVideo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频收藏夹关系表 Mapper 接口
 */
@Mapper
public interface FavoriteVideoMapper extends BaseMapper<FavoriteVideo> {
    // 基础CRUD已由BaseMapper提供
}