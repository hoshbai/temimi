package com.temimi.controller.user;

import com.temimi.model.entity.Favorite;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 获取用户的所有收藏夹
     * GET /api/favorite/list
     */
    @GetMapping("/list")
    public ApiResult<List<Favorite>> getFavorites(@RequestHeader("uid") Integer uid) {
        try {
            List<Favorite> favorites = favoriteService.getFavoritesByUid(uid);
            return ApiResult.success(favorites);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 创建收藏夹
     * POST /api/favorite/create
     */
    @PostMapping("/create")
    public ApiResult<String> createFavorite(@RequestBody Favorite favorite, @RequestHeader("uid") Integer uid) {
        try {
            boolean success = favoriteService.createFavorite(favorite, uid);
            if (success) {
                return ApiResult.success("收藏夹创建成功");
            } else {
                return ApiResult.error("收藏夹创建失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 删除收藏夹 (软删除)
     * DELETE /api/favorite/delete/456
     */
    @DeleteMapping("/delete/{fid}")
    public ApiResult<String> deleteFavorite(@PathVariable Integer fid, @RequestHeader("uid") Integer uid) {
        try {
            boolean success = favoriteService.deleteFavorite(fid, uid);
            if (success) {
                return ApiResult.success("收藏夹删除成功");
            } else {
                return ApiResult.error("收藏夹删除失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}