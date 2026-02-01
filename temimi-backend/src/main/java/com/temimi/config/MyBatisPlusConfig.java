package com.temimi.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 
 * 修复说明：
 * - 配置字段更新策略为 NOT_NULL，避免 null 值覆盖数据库字段
 * - 配置分页插件
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 全局配置
     * ✅ 关键修复：设置字段更新策略为 NOT_NULL
     * 这样 updateById 时只会更新非 null 字段，避免覆盖数据库中的其他字段
     */
    @Bean
    public GlobalConfig globalConfig() {
        GlobalConfig globalConfig = new GlobalConfig();
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        
        // 设置字段更新策略：只更新非 null 字段
        dbConfig.setUpdateStrategy(com.baomidou.mybatisplus.annotation.FieldStrategy.NOT_NULL);
        
        globalConfig.setDbConfig(dbConfig);
        return globalConfig;
    }
}
