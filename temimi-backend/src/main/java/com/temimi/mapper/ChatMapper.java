package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.Chat;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天会话 Mapper
 */
@Mapper
public interface ChatMapper extends BaseMapper<Chat> {
}
