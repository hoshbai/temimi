package com.temimi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.temimi.model.entity.Favorite;
import com.temimi.model.entity.Video;

import java.util.List;

/**
 * 收藏夹服务接口
 */
public interface FavoriteService extends IService<Favorite> {

    /**
     * 根据用户ID获取其所有收藏夹
     * @param uid 用户ID
     * @return 收藏夹列表
     */
    List<Favorite> getFavoritesByUid(Integer uid);

    /**
     * 根据用户ID获取其公开的收藏夹（用于个人空间展示）
     * @param uid 用户ID
     * @return 公开收藏夹列表
     */
    List<Favorite> getPublicFavoritesByUid(Integer uid);

    /**
     * 创建收藏夹
     * @param favorite 收藏夹对象
     * @param uid 用户ID
     * @return 是否创建成功
     */
    boolean createFavorite(Favorite favorite, Integer uid);

    /**
     * 删除收藏夹 (软删除)
     * @param fid 收藏夹ID
     * @param uid 用户ID
     * @return 是否删除成功
     */
    boolean deleteFavorite(Integer fid, Integer uid);

    /**
     * 更新收藏夹信息
     * @param favorite 收藏夹对象（包含fid）
     * @param uid 用户ID
     * @return 是否更新成功
     */
    boolean updateFavoriteInfo(Favorite favorite, Integer uid);

    /**
     * 向收藏夹添加视频
     * @param vid 视频ID
     * @param fid 收藏夹ID
     * @param uid 用户ID
     * @return 是否添加成功
     */
    boolean addVideoToFavorite(Integer vid, Integer fid, Integer uid);

    /**
     * 从收藏夹移除视频
     * @param vid 视频ID
     * @param fid 收藏夹ID
     * @param uid 用户ID
     * @return 是否移除成功
     */
    boolean removeVideoFromFavorite(Integer vid, Integer fid, Integer uid);

    /**
     * 获取收藏夹内的视频列表（分页）
     * @param fid 收藏夹ID
     * @param uid 用户ID（用于权限验证）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 视频分页对象
     */
    Page<Video> getVideosInFavorite(Integer fid, Integer uid, Integer pageNum, Integer pageSize);

    /**
     * 收藏视频到多个收藏夹（支持批量）
     * @param vid 视频ID
     * @param fids 收藏夹ID列表
     * @param uid 用户ID
     * @return 是否全部成功
     */
    boolean collectVideoToFavorites(Integer vid, List<Integer> fids, Integer uid);

    /**
     * 查询视频被收藏到哪些收藏夹
     * @param vid 视频ID
     * @param uid 用户ID
     * @return 收藏夹ID列表
     */
    List<Integer> getCollectedFids(Integer vid, Integer uid);
}