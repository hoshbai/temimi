package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.ChatDetailed;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天详细消息 Mapper
 */
@Mapper
public interface ChatDetailedMapper extends BaseMapper<ChatDetailed> {
}
