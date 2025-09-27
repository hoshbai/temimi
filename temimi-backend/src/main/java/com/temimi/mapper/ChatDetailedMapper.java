package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.ChatDetailed;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天记录表 Mapper 接口
 */
@Mapper
public interface ChatDetailedMapper extends BaseMapper<ChatDetailed> {
    // 基础CRUD已由BaseMapper提供
}