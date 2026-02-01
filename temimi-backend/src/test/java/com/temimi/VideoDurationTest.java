package com.temimi;

import org.junit.jupiter.api.Test;
import org.mp4parser.IsoFile;

import java.io.File;

/**
 * 测试视频时长获取功能
 */
public class VideoDurationTest {

    @Test
    public void testMp4ParserDuration() {
        // 替换为你的实际视频文件路径
        String videoPath = "D:/shiyou_upload/videos/test.mp4";
        
        File videoFile = new File(videoPath);
        if (!videoFile.exists()) {
            System.out.println("❌ 视频文件不存在: " + videoPath);
            System.out.println("请将测试视频文件放到: " + videoPath);
            return;
        }
        
        try {
            System.out.println("📹 开始测试视频时长获取...");
            System.out.println("文件路径: " + videoPath);
            System.out.println("文件大小: " + (videoFile.length() / 1024 / 1024) + " MB");
            
            // 使用 mp4parser 读取视频时长
            IsoFile isoFile = new IsoFile(videoPath);
            double lengthInSeconds = (double) isoFile.getMovieBox().getMovieHeaderBox().getDuration() 
                                   / isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
            isoFile.close();
            
            System.out.println("✅ 成功获取视频时长: " + lengthInSeconds + " 秒");
            System.out.println("格式化时长: " + formatDuration(lengthInSeconds));
            
        } catch (Exception e) {
            System.out.println("❌ 获取视频时长失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test
    public void testFFmpegDuration() {
        // 替换为你的实际视频文件路径
        String videoPath = "D:/shiyou_upload/videos/test.mp4";
        
        File videoFile = new File(videoPath);
        if (!videoFile.exists()) {
            System.out.println("❌ 视频文件不存在: " + videoPath);
            return;
        }
        
        try {
            System.out.println("📹 开始测试 FFmpeg 时长获取...");
            System.out.println("文件路径: " + videoPath);
            
            ProcessBuilder processBuilder = new ProcessBuilder(
                "ffprobe", "-v", "error", "-show_entries", 
                "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", 
                videoPath
            );
            Process process = processBuilder.start();
            
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );
            String line = reader.readLine();
            process.waitFor();
            
            if (line != null && !line.isEmpty()) {
                double duration = Double.parseDouble(line);
                System.out.println("✅ 成功通过 FFmpeg 获取视频时长: " + duration + " 秒");
                System.out.println("格式化时长: " + formatDuration(duration));
            } else {
                System.out.println("❌ FFmpeg 未返回时长数据");
            }
            
        } catch (Exception e) {
            System.out.println("❌ FFmpeg 获取视频时长失败: " + e.getMessage());
            System.out.println("提示: 可能未安装 FFmpeg，这是正常的（mp4parser 可以处理 MP4 文件）");
        }
    }
    
    private String formatDuration(double seconds) {
        int mins = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%d:%02d", mins, secs);
    }
}
