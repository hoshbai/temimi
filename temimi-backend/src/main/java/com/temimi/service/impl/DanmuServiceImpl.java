package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.exception.BusinessException;
import com.temimi.exception.BusinessErrorCode;
import com.temimi.mapper.DanmuMapper;
import com.temimi.mapper.VideoStatsMapper;
import com.temimi.model.entity.Danmu;
import com.temimi.service.DanmuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 弹幕服务实现类
 *
 * 安全特性：
 * - XSS 防护：移除 HTML 标签和危险字符
 * - 长度限制：最大 100 字符
 * - SQL 注入防护：使用 MyBatis-Plus 参数化查询
 */
@Service
public class DanmuServiceImpl extends ServiceImpl<DanmuMapper, Danmu> implements DanmuService {

    private static final Logger logger = LoggerFactory.getLogger(DanmuServiceImpl.class);

    @Autowired
    private DanmuMapper danmuMapper;

    @Autowired
    private VideoStatsMapper videoStatsMapper;

    // ==================== 安全配置常量 ====================

    /**
     * 弹幕内容最大长度（与数据库 VARCHAR(100) 保持一致）
     */
    private static final int MAX_DANMU_LENGTH = 100;

    /**
     * HTML 标签匹配正则（用于 XSS 防护）
     * 匹配所有 <xxx> 形式的标签
     */
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    /**
     * 危险字符正则（script、iframe、onclick 等）
     * 匹配常见的 XSS 攻击向量
     */
    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
        "(?i)(script|iframe|object|embed|applet|meta|link|style|" +
        "javascript:|vbscript:|onclick|onerror|onload|onmouseover|eval|expression)"
    );

    // ==================== 业务方法 ====================

    @Override
    public List<Danmu> getDanmuByVid(Integer vid) {
        QueryWrapper<Danmu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid)
                .eq("state", 1) // 只查询已过审的弹幕
                .orderByAsc("time_point"); // 按视频时间点排序

        return danmuMapper.selectList(queryWrapper);
    }

    @Override
    public List<Danmu> getDanmuByVidAndDate(Integer vid, String date) {
        // 将日期字符串转换为 LocalDateTime 范围
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime startOfDay = LocalDateTime.parse(date + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime endOfDay = LocalDateTime.parse(date + "T23:59:59", DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        QueryWrapper<Danmu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid)
                .eq("state", 1) // 只查询已过审的弹幕
                .between("create_date", startOfDay, endOfDay) // 在指定日期范围内
                .orderByAsc("time_point"); // 按视频时间点排序

        return danmuMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendDanmu(Danmu danmu, Integer uid) {
        try {
            // ========== 1. 参数校验 ==========
            validateDanmuParams(danmu, uid);

            // ========== 2. XSS 过滤与内容清洗 ==========
            String sanitizedContent = sanitizeDanmuContent(danmu.getContent());
            danmu.setContent(sanitizedContent);

            // ========== 3. 设置弹幕属性 ==========
            danmu.setUid(uid);
            danmu.setCreateDate(LocalDateTime.now());
            danmu.setState(1); // 1: 默认过审（可根据需求改为预审）

            // 设置默认样式（如果前端未传）
            if (danmu.getFontsize() == null || danmu.getFontsize() <= 0) {
                danmu.setFontsize(25);
            }
            if (danmu.getMode() == null || danmu.getMode() <= 0) {
                danmu.setMode(1); // 1: 滚动弹幕
            }
            if (danmu.getColor() == null || danmu.getColor().trim().isEmpty()) {
                danmu.setColor("#FFFFFF");
            }

            // ========== 4. 插入数据库 ==========
            int result = danmuMapper.insert(danmu);

            if (result <= 0) {
                logger.error("弹幕插入数据库失败, vid={}, uid={}", danmu.getVid(), uid);
                throw new BusinessException(BusinessErrorCode.DATABASE_ERROR, "弹幕发送失败");
            }

            // ========== 5. 更新视频弹幕统计数 ==========
            try {
                videoStatsMapper.incrementDanmu(danmu.getVid());
            } catch (Exception e) {
                logger.warn("更新视频弹幕统计数失败, vid={}, 原因: {}", danmu.getVid(), e.getMessage());
                // 不影响主流程，仅记录日志
            }

            logger.info("弹幕发送成功, id={}, vid={}, uid={}, content={}",
                danmu.getId(), danmu.getVid(), uid, sanitizedContent);

            return true;

        } catch (BusinessException e) {
            // 业务异常直接抛出
            throw e;
        } catch (Exception e) {
            logger.error("弹幕发送失败, vid={}, uid={}, 原因: {}",
                danmu.getVid(), uid, e.getMessage(), e);
            throw new BusinessException(BusinessErrorCode.DATABASE_ERROR, "弹幕发送失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendDanmu(Danmu danmu) {
        try {
            // ========== 1. 参数校验 ==========
            if (danmu.getUid() == null || danmu.getUid() <= 0) {
                throw new BusinessException(BusinessErrorCode.USER_NOT_FOUND, "用户未登录");
            }

            return sendDanmu(danmu, danmu.getUid());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("弹幕发送失败, vid={}, 原因: {}",
                danmu.getVid(), e.getMessage(), e);
            throw new BusinessException(BusinessErrorCode.DATABASE_ERROR, "弹幕发送失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDanmu(Integer danmuId) {
        try {
            if (danmuId == null || danmuId <= 0) {
                throw new BusinessException(BusinessErrorCode.SYSTEM_ERROR, "弹幕ID无效");
            }

            int result = danmuMapper.deleteById(danmuId);
            return result > 0;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("删除弹幕失败, danmuId={}, 原因: {}", danmuId, e.getMessage(), e);
            throw new BusinessException(BusinessErrorCode.DATABASE_ERROR, "删除弹幕失败");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 校验弹幕参数的合法性
     *
     * @param danmu 弹幕对象
     * @param uid 用户ID
     * @throws BusinessException 参数不合法时抛出
     */
    private void validateDanmuParams(Danmu danmu, Integer uid) {
        // 校验用户ID
        if (uid == null || uid <= 0) {
            throw new BusinessException(BusinessErrorCode.USER_NOT_FOUND, "用户未登录");
        }

        // 校验视频ID
        if (danmu.getVid() == null || danmu.getVid() <= 0) {
            throw new BusinessException(BusinessErrorCode.VIDEO_NOT_FOUND, "视频ID无效");
        }

        // 校验弹幕内容
        if (danmu.getContent() == null || danmu.getContent().trim().isEmpty()) {
            throw new BusinessException(BusinessErrorCode.SYSTEM_ERROR, "弹幕内容不能为空");
        }

        // 校验时间点
        if (danmu.getTimePoint() == null || danmu.getTimePoint() < 0) {
            throw new BusinessException(BusinessErrorCode.SYSTEM_ERROR, "弹幕时间点无效");
        }
    }

    /**
     * XSS 过滤 - 清洗弹幕内容
     *
     * 防护措施：
     * 1. 移除所有 HTML 标签（<script>、<img>、<iframe> 等）
     * 2. 移除危险关键字（javascript:、onclick 等）
     * 3. 限制长度为 100 字符
     * 4. 移除前后空白字符
     *
     * @param rawContent 原始弹幕内容
     * @return 清洗后的安全内容
     * @throws BusinessException 内容不合法时抛出
     */
    private String sanitizeDanmuContent(String rawContent) {
        String content = rawContent.trim();

        // 1. 长度校验（清洗前先检查，避免处理超长内容）
        if (content.length() > MAX_DANMU_LENGTH * 2) {
            throw new BusinessException(
                BusinessErrorCode.SYSTEM_ERROR,
                "弹幕内容过长，最多允许 " + MAX_DANMU_LENGTH + " 字符"
            );
        }

        // 2. 移除 HTML 标签
        // 例如：<script>alert('XSS')</script> -> alert('XSS')
        content = HTML_TAG_PATTERN.matcher(content).replaceAll("");

        // 3. 检测并拒绝危险字符
        // 例如：javascript:alert(1)、onclick=xxx
        if (DANGEROUS_PATTERN.matcher(content).find()) {
            logger.warn("检测到疑似 XSS 攻击的弹幕内容: {}", rawContent);
            throw new BusinessException(
                BusinessErrorCode.SYSTEM_ERROR,
                "弹幕内容包含非法字符，请修改后重试"
            );
        }

        // 4. HTML 实体编码（额外防护层）
        // 将特殊字符转换为 HTML 实体，防止浏览器解析
        content = htmlEncode(content);

        // 5. 最终长度校验（编码后可能变长）
        if (content.length() > MAX_DANMU_LENGTH) {
            throw new BusinessException(
                BusinessErrorCode.SYSTEM_ERROR,
                "弹幕内容过长，最多允许 " + MAX_DANMU_LENGTH + " 字符"
            );
        }

        // 6. 防止空内容（全是空格或被过滤后为空）
        if (content.isEmpty()) {
            throw new BusinessException(
                BusinessErrorCode.SYSTEM_ERROR,
                "弹幕内容不能为空或仅包含特殊字符"
            );
        }

        return content;
    }

    /**
     * HTML 实体编码 - 将特殊字符转换为安全格式
     *
     * 转换规则：
     * - & → &amp;
     * - < → &lt;
     * - > → &gt;
     * - " → &quot;
     * - ' → &#x27;
     *
     * @param input 原始字符串
     * @return 编码后的字符串
     */
    private String htmlEncode(String input) {
        if (input == null) {
            return "";
        }

        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&':
                    encoded.append("&amp;");
                    break;
                case '<':
                    encoded.append("&lt;");
                    break;
                case '>':
                    encoded.append("&gt;");
                    break;
                case '"':
                    encoded.append("&quot;");
                    break;
                case '\'':
                    encoded.append("&#x27;");
                    break;
                case '/':
                    encoded.append("&#x2F;");
                    break;
                default:
                    encoded.append(c);
            }
        }
        return encoded.toString();
    }
}
