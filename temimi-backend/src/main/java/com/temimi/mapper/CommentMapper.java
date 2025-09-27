package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论表 Mapper 接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    // 基础CRUD已由BaseMapper提供
}