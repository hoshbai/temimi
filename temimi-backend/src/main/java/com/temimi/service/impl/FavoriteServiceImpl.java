package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.constant.BusinessConstants;
import com.temimi.exception.BusinessException;
import com.temimi.exception.BusinessErrorCode;
import com.temimi.mapper.FavoriteMapper;
import com.temimi.mapper.FavoriteVideoMapper;
import com.temimi.mapper.VideoMapper;
import com.temimi.model.entity.Favorite;
import com.temimi.model.entity.FavoriteVideo;
import com.temimi.model.entity.Video;
import com.temimi.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private FavoriteVideoMapper favoriteVideoMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Override
    public List<Favorite> getFavoritesByUid(Integer uid) {
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                .eq("is_delete", BusinessConstants.STATS_INITIAL_VALUE)
                .orderByDesc("fid");

        return favoriteMapper.selectList(queryWrapper);
    }

    @Override
    public List<Favorite> getPublicFavoritesByUid(Integer uid) {
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                .eq("is_delete", BusinessConstants.STATS_INITIAL_VALUE)
                .eq("visible", BusinessConstants.FAVORITE_VISIBLE_PUBLIC)
                .orderByDesc("fid");

        return favoriteMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public boolean createFavorite(Favorite favorite, Integer uid) {
        // ✅ 修复：校验 title 字段是否为空
        if (favorite.getTitle() == null || favorite.getTitle().trim().isEmpty()) {
            throw new BusinessException(BusinessErrorCode.SYSTEM_ERROR, "收藏夹标题不能为空");
        }

        // ✅ 修复：限制 title 长度（数据库限制 VARCHAR(20)）
        if (favorite.getTitle().length() > 20) {
            throw new BusinessException(BusinessErrorCode.SYSTEM_ERROR, "收藏夹标题不能超过20个字符");
        }

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
    public boolean updateFavoriteInfo(Favorite favorite, Integer uid) {
        // 1. 检查收藏夹是否存在且属于该用户
        Favorite existing = favoriteMapper.selectById(favorite.getFid());
        if (existing == null || !existing.getUid().equals(uid)) {
            throw new BusinessException(BusinessErrorCode.FAVORITE_PERMISSION_DENIED, "收藏夹不存在或无权操作");
        }

        // 2. 校验标题
        if (favorite.getTitle() != null) {
            if (favorite.getTitle().trim().isEmpty()) {
                throw new BusinessException(BusinessErrorCode.SYSTEM_ERROR, "收藏夹标题不能为空");
            }
            if (favorite.getTitle().length() > 20) {
                throw new BusinessException(BusinessErrorCode.SYSTEM_ERROR, "收藏夹标题不能超过20个字符");
            }
            existing.setTitle(favorite.getTitle());
        }

        // 3. 更新其他可修改字段
        if (favorite.getDescription() != null) {
            existing.setDescription(favorite.getDescription());
        }
        if (favorite.getVisible() != null) {
            existing.setVisible(favorite.getVisible());
        }
        if (favorite.getCover() != null) {
            existing.setCover(favorite.getCover());
        }

        int result = favoriteMapper.updateById(existing);
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

        // 2. 检查视频是否已在该收藏夹中（包括已移除的记录）
        QueryWrapper<FavoriteVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid)
                .eq("fid", fid);

        FavoriteVideo existing = favoriteVideoMapper.selectOne(queryWrapper);
        
        if (existing != null) {
            // 如果记录存在但被标记为已移除，则恢复它
            if (existing.getIsRemove() != null && existing.getIsRemove() == 1) {
                existing.setIsRemove(null);
                existing.setTime(LocalDateTime.now());
                int result = favoriteVideoMapper.updateById(existing);
                
                // 更新收藏夹计数
                if (result > 0) {
                    Favorite updateFavorite = new Favorite();
                    updateFavorite.setFid(fid);
                    updateFavorite.setCount(favorite.getCount() + 1);
                    favoriteMapper.updateById(updateFavorite);
                }
                
                return result > 0;
            } else {
                // 已存在且未被移除，直接返回成功（幂等操作）
                return true;
            }
        }

        // 3. 创建新的收藏记录
        FavoriteVideo favoriteVideo = new FavoriteVideo();
        favoriteVideo.setVid(vid);
        favoriteVideo.setFid(fid);
        favoriteVideo.setTime(LocalDateTime.now());

        try {
            int result = favoriteVideoMapper.insert(favoriteVideo);

            // 4. 在同一事务中更新收藏夹的视频数量
            if (result > 0) {
                Favorite updateFavorite = new Favorite();
                updateFavorite.setFid(fid);
                updateFavorite.setCount(favorite.getCount() + 1);
                int updateResult = favoriteMapper.updateById(updateFavorite);
                if (updateResult <= 0) {
                    throw new BusinessException(BusinessErrorCode.DATABASE_ERROR, "更新收藏夹计数失败");
                }
            }

            return result > 0;
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // ✅ 捕获重复键异常，说明记录已存在（并发情况下可能发生）
            // 这是幂等操作，返回成功即可
            return true;
        }
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
        // ✅ 修复：只更新 count 字段，避免覆盖 cover 等其他字段
        if (result > 0) {
            Favorite updateFavorite = new Favorite();
            updateFavorite.setFid(fid);
            updateFavorite.setCount(Math.max(BusinessConstants.STATS_INITIAL_VALUE, favorite.getCount() - 1));
            int updateResult = favoriteMapper.updateById(updateFavorite);
            if (updateResult <= 0) {
                throw new BusinessException(BusinessErrorCode.DATABASE_ERROR, "更新收藏夹计数失败");
            }
        }

        return result > 0;
    }

    @Override
    public Page<Video> getVideosInFavorite(Integer fid, Integer uid, Integer pageNum, Integer pageSize) {
        // 1. 检查收藏夹权限（如果是私密收藏夹，只有所有者才能查看）
        Favorite favorite = favoriteMapper.selectById(fid);
        if (favorite == null) {
            throw new BusinessException(BusinessErrorCode.FAVORITE_NOT_FOUND, "收藏夹不存在");
        }

        // 如果是私密收藏夹，检查权限
        if (favorite.getVisible() == BusinessConstants.FAVORITE_VISIBLE_PRIVATE &&
            !favorite.getUid().equals(uid)) {
            throw new BusinessException(BusinessErrorCode.FAVORITE_PERMISSION_DENIED, "无权查看该收藏夹");
        }

        // 2. 查询收藏夹中的视频ID列表
        QueryWrapper<FavoriteVideo> fvQueryWrapper = new QueryWrapper<>();
        fvQueryWrapper.eq("fid", fid)
                .isNull("is_remove")
                .orderByDesc("time");

        List<FavoriteVideo> favoriteVideos = favoriteVideoMapper.selectList(fvQueryWrapper);

        if (favoriteVideos.isEmpty()) {
            return new Page<>(pageNum, pageSize);  // 返回空页
        }

        List<Integer> vids = favoriteVideos.stream()
                .map(FavoriteVideo::getVid)
                .collect(Collectors.toList());

        // 3. 分页查询视频信息
        Page<Video> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Video> videoQueryWrapper = new QueryWrapper<>();
        videoQueryWrapper.in("vid", vids)
                .eq("status", BusinessConstants.VIDEO_STATUS_APPROVED)  // 只显示已审核的视频
                .orderByDesc("upload_date");

        return videoMapper.selectPage(page, videoQueryWrapper);
    }

    @Override
    @Transactional
    public boolean collectVideoToFavorites(Integer vid, List<Integer> fids, Integer uid) {
        if (fids == null || fids.isEmpty()) {
            return true;  // 没有要收藏的夹，直接返回成功
        }

        boolean allSuccess = true;
        for (Integer fid : fids) {
            try {
                addVideoToFavorite(vid, fid, uid);
            } catch (Exception e) {
                allSuccess = false;
                // 记录日志但继续处理其他收藏夹
            }
        }

        return allSuccess;
    }

    @Override
    public List<Integer> getCollectedFids(Integer vid, Integer uid) {
        // 1. 获取用户的所有收藏夹
        List<Favorite> favorites = getFavoritesByUid(uid);

        if (favorites.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> allFids = favorites.stream()
                .map(Favorite::getFid)
                .collect(Collectors.toList());

        // 2. 查询视频在这些收藏夹中的收藏记录
        QueryWrapper<FavoriteVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid)
                .in("fid", allFids)
                .isNull("is_remove");

        List<FavoriteVideo> favoriteVideos = favoriteVideoMapper.selectList(queryWrapper);

        // 3. 提取收藏夹ID列表
        return favoriteVideos.stream()
                .map(FavoriteVideo::getFid)
                .collect(Collectors.toList());
    }
}