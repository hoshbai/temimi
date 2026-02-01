package com.temimi.controller.video;

import com.temimi.model.vo.ApiResult;
import com.temimi.service.VideoService;
import com.temimi.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 视频上传控制器
 */
@RestController
@RequestMapping("/api/video")
public class VideoUploadController {

    private static final Logger logger = LoggerFactory.getLogger(VideoUploadController.class);

    @Autowired
    private VideoService videoService;

    // 配置文件中读取
    @Value("${upload.video.max-size:1073741824}") // 默认1GB
    private long maxVideoSize;

    @Value("${upload.cover.max-size:5242880}") // 默认5MB
    private long maxCoverSize;

    // 允许的视频文件类型
    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/avi", "video/mov", "video/wmv", "video/flv"
    );

    // 允许的图片文件类型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif"
    );

    /**
     * 上传视频
     * ✅ 修复：从 SecurityContext 获取 UID，防止伪造
     */
    @PostMapping("/upload")
    public ApiResult<String> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("scId") String scId,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "descr", required = false) String descr,
            @RequestParam(value = "cover", required = false) MultipartFile cover) {

        try {
            // 0. 从 SecurityContext 获取当前登录用户 ID
            Integer uid = SecurityUtil.getCurrentUserIdRequired();

            // 1. 基础参数验证
            validateBasicParams(title, scId);

            // 2. 视频文件验证
            validateVideoFile(file);

            // 3. 封面文件验证（如果有）
            if (cover != null && !cover.isEmpty()) {
                validateCoverFile(cover);
            }

            // 4. 调用Service层处理上传
            String videoUrl = videoService.uploadVideo(file, cover, title, scId, tags, descr, uid);
            return ApiResult.success("视频上传成功，URL: " + videoUrl);

        } catch (IllegalArgumentException e) {
            return ApiResult.error(400, e.getMessage());
        } catch (Exception e) {
            logger.error("视频上传失败: {}", e.getMessage(), e);
            return ApiResult.error("视频上传失败，请稍后重试");
        }
    }

    /**
     * 验证基础参数
     */
    private void validateBasicParams(String title, String scId) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("视频标题不能为空");
        }

        if (title.trim().length() > 80) {
            throw new IllegalArgumentException("视频标题不能超过80个字符");
        }

        if (scId == null || scId.trim().isEmpty()) {
            throw new IllegalArgumentException("请选择视频分区");
        }

        // 防止XSS攻击
        if (containsHtmlTags(title)) {
            throw new IllegalArgumentException("标题不能包含HTML标签");
        }
    }

    /**
     * 验证视频文件
     */
    private void validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("视频文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > maxVideoSize) {
            throw new IllegalArgumentException("视频文件大小不能超过" + (maxVideoSize / 1024 / 1024) + "MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_VIDEO_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("不支持的视频格式，支持格式：MP4、AVI、MOV、WMV、FLV");
        }

        // 检查文件扩展名
        String filename = file.getOriginalFilename();
        if (filename == null || !hasValidVideoExtension(filename)) {
            throw new IllegalArgumentException("视频文件扩展名不正确");
        }

        // 防止路径遍历攻击
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("文件名包含非法字符");
        }
    }

    /**
     * 验证封面文件
     */
    private void validateCoverFile(MultipartFile cover) {
        // 检查文件大小
        if (cover.getSize() > maxCoverSize) {
            throw new IllegalArgumentException("封面文件大小不能超过" + (maxCoverSize / 1024 / 1024) + "MB");
        }

        // 检查文件类型
        String contentType = cover.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("不支持的封面格式，支持格式：JPEG、PNG、GIF");
        }

        // 检查文件扩展名
        String filename = cover.getOriginalFilename();
        if (filename == null || !hasValidImageExtension(filename)) {
            throw new IllegalArgumentException("封面文件扩展名不正确");
        }

        // 防止路径遍历攻击
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("文件名包含非法字符");
        }
    }

    /**
     * 检查是否包含HTML标签
     */
    private boolean containsHtmlTags(String input) {
        return input.matches(".*<[^>]+>.*");
    }

    /**
     * 检查视频文件扩展名
     */
    private boolean hasValidVideoExtension(String filename) {
        String[] validExtensions = {".mp4", ".avi", ".mov", ".wmv", ".flv"};
        String lowerFilename = filename.toLowerCase();
        return Arrays.stream(validExtensions)
                .anyMatch(lowerFilename::endsWith);
    }

    /**
     * 检查图片文件扩展名
     */
    private boolean hasValidImageExtension(String filename) {
        String[] validExtensions = {".jpg", ".jpeg", ".png", ".gif"};
        String lowerFilename = filename.toLowerCase();
        return Arrays.stream(validExtensions)
                .anyMatch(lowerFilename::endsWith);
    }
}