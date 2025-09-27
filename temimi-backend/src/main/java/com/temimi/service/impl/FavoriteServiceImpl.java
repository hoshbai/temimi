package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.constant.BusinessConstants;
import com.temimi.exception.BusinessException;
import com.temimi.exception.BusinessErrorCode;
import com.temimi.mapper.FavoriteMapper;
import com.temimi.mapper.FavoriteVideoMapper;
import com.temimi.model.entity.Favorite;
import com.temimi.model.entity.FavoriteVideo;
import com.temimi.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private FavoriteVideoMapper favoriteVideoMapper;

    @Override
    public List<Favorite> getFavoritesByUid(Integer uid) {
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                .eq("is_delete", BusinessConstants.STATS_INITIAL_VALUE)
                .orderByDesc("fid");

        return favoriteMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public boolean createFavorite(Favorite favorite, Integer uid) {
        favorite.setUid(uid);
        favorite.setType(BusinessConstants.FAVORITE_TYPE_CUSTOM);
        favorite.setVisible(BusinessConstants.FAVORITE_VISIBLE_PUBLIC);
        favorite.setCount(BusinessConstants.STATS_INITIAL_VALUE);
        favorite.setIsDelete(BusinessConstants.STATS_INITIAL_VALUE);

        int result = favoriteMapper.insert(favorite);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean deleteFavorite(Integer fid, Integer uid) {
        Favorite favorite = favoriteMapper.selectById(fid);
        if (favorite == null || !favorite.getUid().equals(uid)) {
            throw new BusinessException(BusinessErrorCode.FAVORITE_PERMISSION_DENIED, "收藏夹不存在或无权删除");
        }

        favorite.setIsDelete(BusinessConstants.USER_ACTION_ACTIVE);
        int result = favoriteMapper.updateById(favorite);

        return result > 0;
    }

    @Override
    @Transactional
    public boolean addVideoToFavorite(Integer vid, Integer fid, Integer uid) {
        // 1. 检查收藏夹是否存在且属于该用户
        Favorite favorite = favoriteMapper.selectById(fid);
        if (favorite == null || !favorite.getUid().equals(uid) || favorite.getIsDelete() == BusinessConstants.USER_ACTION_ACTIVE) {
            throw new BusinessException(BusinessErrorCode.FAVORITE_PERMISSION_DENIED, "收藏夹不存在或无权操作");
        }

        // 2. 检查视频是否已在该收藏夹中
        QueryWrapper<FavoriteVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid)
                .eq("fid", fid)
                .isNull("is_remove");

        FavoriteVideo existing = favoriteVideoMapper.selectOne(queryWrapper);
        if (existing != null) {
            return true; // 已存在，直接返回成功
        }

        // 3. 创建新的收藏记录
        FavoriteVideo favoriteVideo = new FavoriteVideo();
        favoriteVideo.setVid(vid);
        favoriteVideo.setFid(fid);
        favoriteVideo.setTime(LocalDateTime.now());

        int result = favoriteVideoMapper.insert(favoriteVideo);

        // 4. 在同一事务中更新收藏夹的视频数量
        if (result > 0) {
            favorite.setCount(favorite.getCount() + 1);
            int updateResult = favoriteMapper.updateById(favorite);
            if (updateResult <= 0) {
                throw new BusinessException(BusinessErrorCode.DATABASE_ERROR, "更新收藏夹计数失败");
            }
        }

        return result > 0;
    }

    @Override
    @Transactional
    public boolean removeVideoFromFavorite(Integer vid, Integer fid, Integer uid) {
        // 1. 检查收藏夹是否属于该用户
        Favorite favorite = favoriteMapper.selectById(fid);
        if (favorite == null || !favorite.getUid().equals(uid)) {
            throw new BusinessException(BusinessErrorCode.FAVORITE_PERMISSION_DENIED, "收藏夹不存在或无权操作");
        }

        // 2. 查找关联记录
        QueryWrapper<FavoriteVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid)
                .eq("fid", fid)
                .isNull("is_remove");

        FavoriteVideo favoriteVideo = favoriteVideoMapper.selectOne(queryWrapper);
        if (favoriteVideo == null) {
            return true; // 不存在，直接返回成功
        }

        // 3. 标记为已移除
        favoriteVideo.setIsRemove(1);
        int result = favoriteVideoMapper.updateById(favoriteVideo);

        // 4. 在同一事务中更新收藏夹的视频数量
        if (result > 0) {
            favorite.setCount(Math.max(BusinessConstants.STATS_INITIAL_VALUE, favorite.getCount() - 1));
            int updateResult = favoriteMapper.updateById(favorite);
            if (updateResult <= 0) {
                throw new BusinessException(BusinessErrorCode.DATABASE_ERROR, "更新收藏夹计数失败");
            }
        }

        return result > 0;
    }
}