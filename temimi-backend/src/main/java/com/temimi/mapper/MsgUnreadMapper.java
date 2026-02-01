package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.MsgUnread;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息未读数 Mapper
 */
@Mapper
public interface MsgUnreadMapper extends BaseMapper<MsgUnread> {
}
