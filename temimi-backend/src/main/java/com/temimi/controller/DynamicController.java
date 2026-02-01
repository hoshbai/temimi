package com.temimi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.model.entity.Dynamic;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.DynamicService;
import com.temimi.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 动态控制器
 */
@RestController
@RequestMapping({"/api/dynamic", "/dynamic"})
public class DynamicController {

    private static final Logger logger = LoggerFactory.getLogger(DynamicController.class);

    @Autowired
    private DynamicService dynamicService;

    /**
     * 发布动态
     */
    @PostMapping("/publish")
    public ApiResult<Dynamic> publish(@RequestBody Map<String, Object> params) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            Integer type = (Integer) params.get("type");
            String content = (String) params.get("content");
            String images = (String) params.get("images");
            Integer vid = params.get("vid") != null ? (Integer) params.get("vid") : null;
            
            if (type == null || content == null || content.trim().isEmpty()) {
                return ApiResult.error("参数不完整");
            }
            
            Dynamic dynamic = dynamicService.publish(uid, type, content.trim(), images, vid);
            return ApiResult.success(dynamic);
        } catch (SecurityException e) {
            return ApiResult.error(401, e.getMessage());
        } catch (Exception e) {
            logger.error("发布动态失败", e);
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取用户动态列表
     * @param type 动态类型筛选（可选，5=视频）
     */
    @GetMapping("/user/{uid}")
    public ApiResult<Map<String, Object>> getUserDynamics(
            @PathVariable Integer uid,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type) {
        try {
            Integer currentUid = SecurityUtil.getCurrentUserId();
            Page<Dynamic> result = dynamicService.getUserDynamics(uid, page, size, currentUid, type);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", result.getRecords());
            data.put("total", result.getTotal());
            data.put("pages", result.getPages());
            data.put("current", result.getCurrent());
            
            return ApiResult.success(data);
        } catch (Exception e) {
            logger.error("获取用户动态失败", e);
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取关注用户的动态流
     */
    @GetMapping("/following")
    public ApiResult<Map<String, Object>> getFollowingDynamics(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            Page<Dynamic> result = dynamicService.getFollowingDynamics(uid, page, size);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", result.getRecords());
            data.put("total", result.getTotal());
            data.put("pages", result.getPages());
            data.put("current", result.getCurrent());
            
            return ApiResult.success(data);
        } catch (SecurityException e) {
            return ApiResult.error(401, e.getMessage());
        } catch (Exception e) {
            logger.error("获取动态流失败", e);
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取动态详情
     */
    @GetMapping("/detail/{id}")
    public ApiResult<Dynamic> getDetail(@PathVariable Integer id) {
        try {
            Integer currentUid = SecurityUtil.getCurrentUserId();
            Dynamic dynamic = dynamicService.getDetail(id, currentUid);
            if (dynamic == null) {
                return ApiResult.error("动态不存在");
            }
            return ApiResult.success(dynamic);
        } catch (Exception e) {
            logger.error("获取动态详情失败", e);
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/like/{id}")
    public ApiResult<Map<String, Object>> toggleLike(@PathVariable Integer id) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean isLiked = dynamicService.toggleLike(id, uid);
            
            Map<String, Object> data = new HashMap<>();
            data.put("isLiked", isLiked);
            
            return ApiResult.success(data);
        } catch (SecurityException e) {
            return ApiResult.error(401, e.getMessage());
        } catch (Exception e) {
            logger.error("点赞操作失败", e);
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 删除动态
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> deleteDynamic(@PathVariable Integer id) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean success = dynamicService.deleteDynamic(id, uid);
            if (!success) {
                return ApiResult.error("删除失败，动态不存在或无权限");
            }
            return ApiResult.success(null);
        } catch (SecurityException e) {
            return ApiResult.error(401, e.getMessage());
        } catch (Exception e) {
            logger.error("删除动态失败", e);
            return ApiResult.error(e.getMessage());
        }
    }
}
