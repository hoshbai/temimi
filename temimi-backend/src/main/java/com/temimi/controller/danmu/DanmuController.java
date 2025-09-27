package com.temimi.controller.danmu;

import com.temimi.model.entity.Danmu;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.DanmuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/danmu")
public class DanmuController {

    @Autowired
    private DanmuService danmuService;

    /**
     * 根据视频ID和日期获取历史弹幕
     * GET /api/danmu/history?vid=123&date=2025-09-08
     */
    @GetMapping("/history")
    public ApiResult<List<Danmu>> getDanmuHistory(@RequestParam Integer vid, @RequestParam String date) {
        try {
            // 验证日期格式
            LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<Danmu> danmuList = danmuService.getDanmuByVidAndDate(vid, date);
            return ApiResult.success(danmuList);
        } catch (Exception e) {
            return ApiResult.error("日期格式错误或查询失败: " + e.getMessage());
        }
    }
}