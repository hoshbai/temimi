package com.temimi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class
})
@MapperScan("com.temimi.mapper")
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
        System.out.println("========================================");
        System.out.println("✅ 视频平台后端服务启动成功！");
        System.out.println("🏠 访问地址: http://localhost:8080");
        System.out.println("📚 API文档: http://localhost:8080/swagger-ui.html");
        System.out.println("========================================");
    }
}