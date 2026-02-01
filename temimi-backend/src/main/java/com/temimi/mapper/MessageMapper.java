package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息表 Mapper 接口
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    // 基础CRUD已由BaseMapper提供
}
