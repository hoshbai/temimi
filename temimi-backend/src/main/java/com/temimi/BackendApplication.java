package com.temimi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@MapperScan("com.temimi.mapper")
public class BackendApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BackendApplication.class, args);

        System.out.println("========================================");
        System.out.println("✅ 应用启动成功！");
        System.out.println("🏠 访问地址: http://localhost:8080");
        System.out.println("========================================");

        // ========== 1. 扫描 @RestController ==========
        String[] restControllers = context.getBeanNamesForType(RestController.class);
        System.out.println("\n🔍 一、@RestController 数量: " + restControllers.length);
        for (String beanName : restControllers) {
            Object bean = context.getBean(beanName);
            System.out.println("   🟢 Bean 名称: " + beanName + " | 类: " + bean.getClass().getName());
        }

        // ========== 2. 扫描 @Controller（可能带 @ResponseBody）==========
        String[] controllers = context.getBeanNamesForType(Controller.class);
        System.out.println("\n🔍 二、@Controller 数量: " + controllers.length);
        for (String beanName : controllers) {
            Object bean = context.getBean(beanName);
            System.out.println("   🟡 Bean 名称: " + beanName + " | 类: " + bean.getClass().getName());
        }

        // ========== 3. 扫描所有包含 'Controller' 字样的 Bean（兜底）==========
        System.out.println("\n🔍 三、名称含 'Controller' 的所有 Bean:");
        String[] allBeans = context.getBeanDefinitionNames();
        int foundCount = 0;
        for (String beanName : allBeans) {
            if (beanName.toLowerCase().contains("controller")) {
                try {
                    Object bean = context.getBean(beanName);
                    System.out.println("   🔵 Bean 名称: " + beanName + " | 类: " + bean.getClass().getName());
                    foundCount++;
                } catch (Exception e) {
                    System.out.println("   ⚠️ Bean 名称: " + beanName + " | 获取失败: " + e.getMessage());
                }
            }
        }
        if (foundCount == 0) {
            System.out.println("   ❌ 未找到任何名称包含 'controller' 的 Bean");
        }

        // ========== 4. 打印 basePackages 扫描路径（可选）==========
        System.out.println("\n📁 Spring Boot 默认扫描包: " + BackendApplication.class.getPackage().getName());
        // ========== 终极验证 ==========
        System.out.println("\n🧪 终极验证：检查 VideoReviewController 的注解");
        try {
            Object bean = context.getBean("videoReviewController");
            Class<?> clazz = bean.getClass();
            // 跳过CGLIB代理
            while (clazz.getName().contains("$$")) {
                clazz = clazz.getSuperclass();
            }
            boolean hasRestController = clazz.isAnnotationPresent(RestController.class);
            System.out.println("VideoReviewController 是否有 @RestController 注解: " + hasRestController);

            if (!hasRestController) {
                System.out.println("❌ 注解丢失！请检查依赖冲突！");
                for (var ann : clazz.getAnnotations()) {
                    System.out.println("   存在注解: " + ann.annotationType().getName());
                }
            } else {
                System.out.println("✅ 注解正常！问题已修复！");
            }
        } catch (Exception e) {
            System.out.println("验证失败: " + e.getMessage());
        }
    }
}