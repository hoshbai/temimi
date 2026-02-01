package com.temimi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置
 * 用于配置静态资源映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:D:/shiyou_upload/}")
    private String uploadBasePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射头像资源
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:" + uploadBasePath + "avatars/");

        // 映射视频封面资源
        registry.addResourceHandler("/covers/**")
                .addResourceLocations("file:" + uploadBasePath + "covers/");

        // 映射视频资源
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:" + uploadBasePath + "videos/");

        // 映射背景图资源
        registry.addResourceHandler("/backgrounds/**")
                .addResourceLocations("file:" + uploadBasePath + "backgrounds/");
    }
}
