package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 视频表 Mapper 接口
 */
@Mapper
public interface VideoMapper extends BaseMapper<Video> {

    /**
     * 根据分区ID查询视频列表（用于首页推荐）
     * @param mcId 主分区ID
     * @param scId 子分区ID
     * @return 视频列表
     */
    @Select("SELECT * FROM video WHERE mc_id = #{mcId} AND sc_id = #{scId} AND status = 1 ORDER BY upload_date DESC LIMIT 10")
    List<Video> selectByCategory(@Param("mcId") String mcId, @Param("scId") String scId);

    /**
     * 根据标题或标签模糊搜索视频
     * @param keyword 搜索关键词
     * @return 视频列表
     */
    @Select("SELECT * FROM video WHERE (title LIKE CONCAT('%', #{keyword}, '%') OR tags LIKE CONCAT('%', #{keyword}, '%')) AND status = 1")
    List<Video> searchVideos(@Param("keyword") String keyword);
}