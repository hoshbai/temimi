-- ----------------------------
-- Table structure for dynamic (动态表)
-- ----------------------------
DROP TABLE IF EXISTS `dynamic`;
CREATE TABLE `dynamic` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '动态ID',
  `uid` int NOT NULL COMMENT '发布者用户ID',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '动态类型 1纯文字 2图文 3转发视频 4转发文章 5投稿视频',
  `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '动态内容',
  `images` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片URL列表（JSON数组格式）',
  `vid` int NULL DEFAULT NULL COMMENT '关联的视频ID（转发视频或投稿视频时使用）',
  `love` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int NOT NULL DEFAULT 0 COMMENT '评论数',
  `forward_count` int NOT NULL DEFAULT 0 COMMENT '转发数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0正常 1审核中 2已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_uid`(`uid` ASC) USING BTREE,
  INDEX `idx_vid`(`vid` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '动态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dynamic_like (动态点赞表)
-- ----------------------------
DROP TABLE IF EXISTS `dynamic_like`;
CREATE TABLE `dynamic_like` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `dynamic_id` int NOT NULL COMMENT '动态ID',
  `uid` int NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime NOT NULL COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dynamic_uid`(`dynamic_id` ASC, `uid` ASC) USING BTREE,
  INDEX `idx_uid`(`uid` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '动态点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 为已审核通过的视频创建动态记录
-- ----------------------------
INSERT INTO `dynamic` (`uid`, `type`, `vid`, `love`, `comment_count`, `forward_count`, `status`, `create_time`, `update_time`)
SELECT `uid`, 5, `vid`, 0, 0, 0, 0, `create_time`, `update_time`
FROM `video`
WHERE `status` = 1
AND `vid` NOT IN (SELECT `vid` FROM `dynamic` WHERE `vid` IS NOT NULL);
