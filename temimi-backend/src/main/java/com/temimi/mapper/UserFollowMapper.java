package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户关注Mapper接口
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    /**
     * 统计用户的粉丝数量
     * @param uid 用户ID
     * @return 粉丝数量
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE following_id = #{uid} AND status = 1")
    Integer countFans(@Param("uid") Integer uid);

    /**
     * 统计用户的关注数量
     * @param uid 用户ID
     * @return 关注数量
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{uid} AND status = 1")
    Integer countFollowing(@Param("uid") Integer uid);
}
