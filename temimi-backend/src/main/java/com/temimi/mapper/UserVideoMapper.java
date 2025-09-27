package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.UserVideo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户视频关联表 Mapper 接口
 */
@Mapper
public interface UserVideoMapper extends BaseMapper<UserVideo> {

    /**
     * 根据用户ID和视频ID查询关联记录
     * @param uid 用户ID
     * @param vid 视频ID
     * @return 关联记录
     */
    @Select("SELECT * FROM user_video WHERE uid = #{uid} AND vid = #{vid}")
    UserVideo selectByUidAndVid(@Param("uid") Integer uid, @Param("vid") Integer vid);
}