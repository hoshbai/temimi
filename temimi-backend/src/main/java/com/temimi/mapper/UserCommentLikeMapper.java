package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.UserCommentLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户评论点赞点踩Mapper接口
 */
@Mapper
public interface UserCommentLikeMapper extends BaseMapper<UserCommentLike> {
}
