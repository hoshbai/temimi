package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.Chat;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天表 Mapper 接口
 */
@Mapper
public interface ChatMapper extends BaseMapper<Chat> {
    // 基础CRUD已由BaseMapper提供
}