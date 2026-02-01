-- 动态表
-- 请在 shiyou 数据库中执行此SQL

CREATE TABLE IF NOT EXISTS `dynamic` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '动态ID',
    `uid` int NOT NULL COMMENT '发布者用户ID',
    `type` tinyint NOT NULL DEFAULT 1 COMMENT '动态类型 1纯文字 2图文 3转发视频 4转发文章',
    `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '动态内容',
    `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '图片URL列表（JSON数组格式）',
    `vid` int DEFAULT NULL COMMENT '关联的视频ID（转发视频时使用）',
    `love` int NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` int NOT NULL DEFAULT 0 COMMENT '评论数',
    `forward_count` int NOT NULL DEFAULT 0 COMMENT '转发数',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0正常 1审核中 2已删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_uid` (`uid`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE,
    INDEX `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='动态表';

-- 动态点赞表
CREATE TABLE IF NOT EXISTS `dynamic_like` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dynamic_id` int NOT NULL COMMENT '动态ID',
    `uid` int NOT NULL COMMENT '用户ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_dynamic_user` (`dynamic_id`, `uid`) USING BTREE,
    INDEX `idx_dynamic_id` (`dynamic_id`) USING BTREE,
    INDEX `idx_uid` (`uid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='动态点赞表';
