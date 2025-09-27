package com.temimi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.temimi.model.entity.Favorite;

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
}