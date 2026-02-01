package com.temimi.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.model.entity.Danmu;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.DanmuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员弹幕管理控制器
 */
@RestController
@RequestMapping("/api/admin/danmu")
@PreAuthorize("hasRole('ADMIN')")
public class DanmuAdminController {

    @Autowired
    private DanmuService danmuService;

    /**
     * 获取所有弹幕列表（分页）
     * GET /api/admin/danmu/list?page=1&pageSize=20&videoId=123&keyword=xxx
     */
    @GetMapping("/list")
    public ApiResult<Page<Danmu>> getDanmuList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer videoId,
            @RequestParam(required = false) String keyword) {
        try {
            Page<Danmu> danmuPage = new Page<>(page, pageSize);
            QueryWrapper<Danmu> queryWrapper = new QueryWrapper<>();
            
            if (videoId != null) {
                queryWrapper.eq("vid", videoId);
            }
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.like("content", keyword);
            }
            
            queryWrapper.orderByDesc("send_time");
            
            Page<Danmu> result = danmuService.page(danmuPage, queryWrapper);
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error("获取弹幕列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定视频的弹幕列表
     * GET /api/admin/danmu/video/123
     */
    @GetMapping("/video/{videoId}")
    public ApiResult<List<Danmu>> getVideoDanmu(@PathVariable Integer videoId) {
        try {
            List<Danmu> danmuList = danmuService.getDanmuByVid(videoId);
            return ApiResult.success(danmuList);
        } catch (Exception e) {
            return ApiResult.error("获取视频弹幕失败: " + e.getMessage());
        }
    }

    /**
     * 删除弹幕
     * DELETE /api/admin/danmu/{danmuId}
     */
    @DeleteMapping("/{danmuId}")
    public ApiResult<String> deleteDanmu(@PathVariable Integer danmuId) {
        try {
            boolean success = danmuService.deleteDanmu(danmuId);
            if (success) {
                return ApiResult.success("删除成功");
            } else {
                return ApiResult.error("删除失败");
            }
        } catch (Exception e) {
            return ApiResult.error("删除弹幕失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除弹幕
     * POST /api/admin/danmu/batch-delete
     */
    @PostMapping("/batch-delete")
    public ApiResult<String> batchDeleteDanmu(@RequestBody List<Integer> ids) {
        try {
            boolean success = danmuService.removeByIds(ids);
            if (success) {
                return ApiResult.success("批量删除成功");
            } else {
                return ApiResult.error("批量删除失败");
            }
        } catch (Exception e) {
            return ApiResult.error("批量删除弹幕失败: " + e.getMessage());
        }
    }
}
