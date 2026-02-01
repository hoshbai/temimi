package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.constant.BusinessConstants;
import com.temimi.exception.BusinessException;
import com.temimi.exception.BusinessErrorCode;
import com.temimi.mapper.CommentMapper;
import com.temimi.mapper.UserCommentLikeMapper;
import com.temimi.model.entity.Comment;
import com.temimi.model.entity.CommentTree;
import com.temimi.model.entity.Message;
import com.temimi.model.entity.User;
import com.temimi.model.entity.UserCommentLike;
import com.temimi.service.CommentService;
import com.temimi.service.MessageService;
import com.temimi.service.UserService;
import com.temimi.service.VideoStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserCommentLikeMapper userCommentLikeMapper;

    @Autowired
    private VideoStatsService videoStatsService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private com.temimi.service.MsgUnreadService msgUnreadService;

    @Override
    public List<Comment> getRootCommentsByVid(Integer vid, int pageNum, int pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid)
                .eq("root_id", BusinessConstants.COMMENT_ROOT_ID)
                .eq("is_deleted", Boolean.FALSE) // ✅ 使用 Boolean.FALSE
                .orderByDesc("create_time");

        return commentMapper.selectPage(page, queryWrapper).getRecords();
    }

    /**
     * ✅ 新增方法：返回Page对象，并填充用户信息
     */
    @Override
    public Page<Comment> getRootCommentsByVidPage(Integer vid, int pageNum, int pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid)
                .eq("root_id", BusinessConstants.COMMENT_ROOT_ID)
                .eq("is_deleted", Boolean.FALSE)
                .orderByDesc("create_time");

        Page<Comment> resultPage = commentMapper.selectPage(page, queryWrapper);

        // ✅ 填充用户信息（昵称和头像）
        for (Comment comment : resultPage.getRecords()) {
            fillUserInfo(comment);
        }

        return resultPage;
    }

    /**
     * ✅ 填充评论的用户信息
     */
    private void fillUserInfo(Comment comment) {
        if (comment.getUid() != null) {
            User user = userService.getUserByIdSafe(comment.getUid());
            if (user != null) {
                comment.setUsername(user.getNickname());
                comment.setUserAvatar(user.getAvatar());
            } else {
                comment.setUsername("未知用户");
                comment.setUserAvatar(BusinessConstants.DEFAULT_AVATAR_PATH);
            }
        } else {
            comment.setUsername("未知用户");
            comment.setUserAvatar(BusinessConstants.DEFAULT_AVATAR_PATH);
        }
    }

    @Override
    public CommentTree getCommentTreeByRootId(Integer rootId) {
        Comment rootComment = commentMapper.selectById(rootId);
        // ✅ 修复：安全判断 isDeleted
        if (rootComment == null || Boolean.TRUE.equals(rootComment.getIsDeleted())) {
            return null;
        }

        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("root_id", rootId)
                .eq("is_deleted", Boolean.FALSE) // ✅ Boolean.FALSE
                .orderByAsc("create_time");

        List<Comment> childComments = commentMapper.selectList(queryWrapper);

        CommentTree commentTree = new CommentTree();
        commentTree.setId(rootComment.getId());
        commentTree.setVid(rootComment.getVid());
        commentTree.setUid(rootComment.getUid());
        commentTree.setContent(rootComment.getContent());
        commentTree.setLove(rootComment.getLove());
        commentTree.setNickname("用户" + rootComment.getUid());
        commentTree.setAvatar(BusinessConstants.DEFAULT_AVATAR_PATH);
        commentTree.setCreateTime(rootComment.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        commentTree.setIsTop(Boolean.TRUE.equals(rootComment.getIsTop())); // ✅ 安全处理 Boolean
        commentTree.setChildren(null);

        return commentTree;
    }

    @Override
    @Transactional
    public boolean postComment(Comment comment, Integer uid) {
        try {
            log.info("开始发布评论 - uid: {}, vid: {}, parent_id: {}, root_id: {}",
                uid, comment.getVid(), comment.getParentId(), comment.getRootId());

            comment.setUid(uid);
            comment.setCreateTime(LocalDateTime.now());
            comment.setLove(BusinessConstants.STATS_INITIAL_VALUE);
            comment.setBad(BusinessConstants.STATS_INITIAL_VALUE);
            comment.setIsTop(Boolean.FALSE);      // ✅ 明确使用 Boolean.FALSE
            comment.setIsDeleted(Boolean.FALSE);  // ✅ 明确使用 Boolean.FALSE

            handleCommentHierarchy(comment);

            log.info("评论层级处理完成 - root_id: {}, parent_id: {}, to_user_id: {}",
                comment.getRootId(), comment.getParentId(), comment.getToUserId());

            int result = commentMapper.insert(comment);

            log.info("评论插入数据库 - result: {}, comment_id: {}", result, comment.getId());

            if (result > 0) {
                // ✅ 确保MyBatis已填充自增ID
                if (comment.getId() == null) {
                    log.error("评论ID未生成，无法创建消息通知 - vid: {}", comment.getVid());
                    throw new BusinessException(BusinessErrorCode.SYSTEM_ERROR, "评论ID生成失败");
                }

                try {
                    videoStatsService.incrementComment(comment.getVid());
                    log.info("视频评论数已更新 - vid: {}", comment.getVid());
                } catch (Exception e) {
                    log.error("更新视频评论数失败: {}", e.getMessage(), e);
                    // ✅ 不影响评论发布，不重新抛出异常
                }

                // ✅ 处理未读消息通知
                try {
                    handleUnreadNotification(comment, uid);
                } catch (Exception e) {
                    log.error("未读消息通知失败（不影响评论发布）: {}", e.getMessage(), e);
                    // 忽略异常，不影响评论发布
                }
            }

            return result > 0;
        } catch (Exception e) {
            log.error("发布评论失败 - uid: {}, vid: {}, error: {}", uid, comment.getVid(), e.getMessage(), e);
            throw e; // 重新抛出异常，让事务回滚
        }
    }
    
    /**
     * 处理评论的未读消息通知
     * - 如果是回复评论，创建"回复"类型消息
     * - 如果评论内容包含@用户，创建"@"类型消息
     */
    private void handleUnreadNotification(Comment comment, Integer uid) {
        try {
            // 1. 处理回复通知
            if (comment.getToUserId() != null && comment.getToUserId() != 0 && !comment.getToUserId().equals(uid)) {
                // 回复了其他用户的评论，创建回复消息
                Message replyMessage = new Message();
                replyMessage.setFromUid(uid);
                replyMessage.setToUid(comment.getToUserId());
                replyMessage.setType("reply");
                replyMessage.setContent(truncateContent(comment.getContent(), 100)); // 截取前100字符
                replyMessage.setTargetType("comment");
                replyMessage.setTargetId(comment.getId());
                replyMessage.setTargetContent(truncateContent(comment.getContent(), 50));
                replyMessage.setIsRead(false);
                replyMessage.setCreateTime(LocalDateTime.now());

                messageService.createMessage(replyMessage);
            }

            // 2. 处理@通知 - 解析评论内容中的@用户
            if (comment.getContent() != null && comment.getContent().contains("@")) {
                java.util.List<Integer> mentionedUserIds = extractMentionedUsers(comment.getContent());
                for (Integer mentionedUid : mentionedUserIds) {
                    // 不给自己发消息，也不给已经收到回复消息的用户重复发送@消息
                    if (!mentionedUid.equals(uid) && !mentionedUid.equals(comment.getToUserId())) {
                        // 创建@消息
                        Message atMessage = new Message();
                        atMessage.setFromUid(uid);
                        atMessage.setToUid(mentionedUid);
                        atMessage.setType("at");
                        atMessage.setContent(truncateContent(comment.getContent(), 100));
                        atMessage.setTargetType("comment");
                        atMessage.setTargetId(comment.getId());
                        atMessage.setTargetContent(truncateContent(comment.getContent(), 50));
                        atMessage.setIsRead(false);
                        atMessage.setCreateTime(LocalDateTime.now());

                        messageService.createMessage(atMessage);
                    }
                }
            }
        } catch (Exception e) {
            // 未读消息通知失败不影响评论发布
            log.error("处理评论未读通知失败: {}", e.getMessage());
        }
    }

    /**
     * 截取字符串，避免消息内容过长
     * ✅ 改进：处理null、空字符串、负数等边界情况
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        if (maxLength <= 0) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        // ✅ 避免截取emoji等多字节字符导致的乱码
        // 使用codePoint而不是char来正确处理Unicode
        if (content.codePointCount(0, content.length()) <= maxLength) {
            return content;
        }

        // 截取到最接近maxLength的完整字符位置
        int endIndex = content.offsetByCodePoints(0, Math.min(maxLength, content.codePointCount(0, content.length())));
        return content.substring(0, endIndex) + "...";
    }
    
    /**
     * 从评论内容中提取被@的用户ID
     * 支持格式: @[用户名](uid) 或 @(uid)
     * 不支持 @123 这种格式，必须有括号
     */
    private java.util.List<Integer> extractMentionedUsers(String content) {
        java.util.List<Integer> userIds = new java.util.ArrayList<>();
        // ✅ 更严格的正则：必须有括号，可选方括号包裹用户名
        // 匹配: @[用户名](123) 或 @(123)，但不匹配 @123
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("@(?:\\[([^\\]]+)\\])?\\((\\d+)\\)");
        java.util.regex.Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            try {
                String uidStr = matcher.group(2);
                if (uidStr != null && !uidStr.isEmpty()) {
                    Integer uid = Integer.parseInt(uidStr);
                    // ✅ 验证UID的合理性（大于0）
                    if (uid > 0 && !userIds.contains(uid)) {
                        userIds.add(uid);
                    }
                }
            } catch (NumberFormatException e) {
                log.warn("解析@用户ID失败: {}", matcher.group());
            }
        }
        
        return userIds;
    }

    @Override
    @Transactional
    public boolean likeComment(Integer commentId, Integer uid) {
        // 检查评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || Boolean.TRUE.equals(comment.getIsDeleted())) {
            throw new BusinessException(BusinessErrorCode.COMMENT_NOT_FOUND, "评论不存在或已被删除");
        }

        // 检查是否已经点赞
        QueryWrapper<UserCommentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                .eq("comment_id", commentId);
        UserCommentLike existing = userCommentLikeMapper.selectOne(queryWrapper);

        if (existing != null) {
            if (UserCommentLike.TYPE_LIKE.equals(existing.getType())) {
                // 已经点赞，不做任何操作
                return true;
            } else {
                // 之前是点踩，先取消点踩，然后点赞
                comment.setBad(Math.max(0, comment.getBad() - 1));
                existing.setType(UserCommentLike.TYPE_LIKE);
                existing.setCreateTime(LocalDateTime.now());
                userCommentLikeMapper.updateById(existing);
            }
        } else {
            // 新增点赞记录
            UserCommentLike like = new UserCommentLike();
            like.setUid(uid);
            like.setCommentId(commentId);
            like.setType(UserCommentLike.TYPE_LIKE);
            like.setCreateTime(LocalDateTime.now());
            userCommentLikeMapper.insert(like);
        }

        // 更新评论的点赞数
        comment.setLove(comment.getLove() + 1);
        int result = commentMapper.updateById(comment);

        // 创建点赞消息（不给自己发消息）
        if (result > 0 && !uid.equals(comment.getUid())) {
            try {
                Message likeMessage = new Message();
                likeMessage.setFromUid(uid);
                likeMessage.setToUid(comment.getUid());
                likeMessage.setType("like");
                likeMessage.setContent("赞了你的评论");
                likeMessage.setTargetType("comment");
                likeMessage.setTargetId(commentId);
                likeMessage.setTargetContent(truncateContent(comment.getContent(), 50));
                likeMessage.setIsRead(false);
                likeMessage.setCreateTime(LocalDateTime.now());

                messageService.createMessage(likeMessage);
            } catch (Exception e) {
                log.error("创建点赞消息失败（不影响点赞操作）: {}", e.getMessage());
                // 忽略异常，不影响点赞功能
            }
        }

        return result > 0;
    }

    @Override
    @Transactional
    public boolean unlikeComment(Integer commentId, Integer uid) {
        // 检查评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || Boolean.TRUE.equals(comment.getIsDeleted())) {
            throw new BusinessException(BusinessErrorCode.COMMENT_NOT_FOUND, "评论不存在或已被删除");
        }

        // 检查是否已经点赞
        QueryWrapper<UserCommentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                .eq("comment_id", commentId)
                .eq("type", UserCommentLike.TYPE_LIKE);
        UserCommentLike existing = userCommentLikeMapper.selectOne(queryWrapper);

        if (existing == null) {
            // 没有点赞记录，不做任何操作
            return true;
        }

        // 删除点赞记录
        userCommentLikeMapper.deleteById(existing.getId());

        // 更新评论的点赞数
        int newLoveCount = Math.max(BusinessConstants.STATS_INITIAL_VALUE, comment.getLove() - 1);
        comment.setLove(newLoveCount);
        int result = commentMapper.updateById(comment);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean dislikeComment(Integer commentId, Integer uid) {
        // 检查评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || Boolean.TRUE.equals(comment.getIsDeleted())) {
            throw new BusinessException(BusinessErrorCode.COMMENT_NOT_FOUND, "评论不存在或已被删除");
        }

        // 检查是否已经有点赞/点踩记录
        QueryWrapper<UserCommentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                .eq("comment_id", commentId);
        UserCommentLike existing = userCommentLikeMapper.selectOne(queryWrapper);

        if (existing != null) {
            if (UserCommentLike.TYPE_DISLIKE.equals(existing.getType())) {
                // 已经点踩，不做任何操作
                return true;
            } else {
                // 之前是点赞，先取消点赞，然后点踩
                comment.setLove(Math.max(0, comment.getLove() - 1));
                existing.setType(UserCommentLike.TYPE_DISLIKE);
                existing.setCreateTime(LocalDateTime.now());
                userCommentLikeMapper.updateById(existing);
            }
        } else {
            // 新增点踩记录
            UserCommentLike dislike = new UserCommentLike();
            dislike.setUid(uid);
            dislike.setCommentId(commentId);
            dislike.setType(UserCommentLike.TYPE_DISLIKE);
            dislike.setCreateTime(LocalDateTime.now());
            userCommentLikeMapper.insert(dislike);
        }

        // 更新评论的点踩数
        if (comment.getBad() == null) {
            comment.setBad(0);
        }
        comment.setBad(comment.getBad() + 1);
        int result = commentMapper.updateById(comment);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean undislikeComment(Integer commentId, Integer uid) {
        // 检查评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || Boolean.TRUE.equals(comment.getIsDeleted())) {
            throw new BusinessException(BusinessErrorCode.COMMENT_NOT_FOUND, "评论不存在或已被删除");
        }

        // 检查是否已经点踩
        QueryWrapper<UserCommentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                .eq("comment_id", commentId)
                .eq("type", UserCommentLike.TYPE_DISLIKE);
        UserCommentLike existing = userCommentLikeMapper.selectOne(queryWrapper);

        if (existing == null) {
            // 没有点踩记录，不做任何操作
            return true;
        }

        // 删除点踩记录
        userCommentLikeMapper.deleteById(existing.getId());

        // 更新评论的点踩数
        int newBadCount = Math.max(0, (comment.getBad() != null ? comment.getBad() : 0) - 1);
        comment.setBad(newBadCount);
        int result = commentMapper.updateById(comment);
        return result > 0;
    }

    @Override
    public List<Integer> getUserLikedComments(Integer uid, Integer vid) {
        QueryWrapper<UserCommentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                .eq("type", UserCommentLike.TYPE_LIKE);

        // 如果提供了视频ID，则只查询该视频下的评论
        if (vid != null) {
            // 需要关联comment表查询
            List<UserCommentLike> likes = userCommentLikeMapper.selectList(queryWrapper);
            return likes.stream()
                    .map(UserCommentLike::getCommentId)
                    .filter(commentId -> {
                        Comment comment = commentMapper.selectById(commentId);
                        return comment != null && vid.equals(comment.getVid());
                    })
                    .toList();
        }

        List<UserCommentLike> likes = userCommentLikeMapper.selectList(queryWrapper);
        return likes.stream().map(UserCommentLike::getCommentId).toList();
    }

    @Override
    public List<Integer> getUserDislikedComments(Integer uid, Integer vid) {
        QueryWrapper<UserCommentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                .eq("type", UserCommentLike.TYPE_DISLIKE);

        // 如果提供了视频ID，则只查询该视频下的评论
        if (vid != null) {
            // 需要关联comment表查询
            List<UserCommentLike> dislikes = userCommentLikeMapper.selectList(queryWrapper);
            return dislikes.stream()
                    .map(UserCommentLike::getCommentId)
                    .filter(commentId -> {
                        Comment comment = commentMapper.selectById(commentId);
                        return comment != null && vid.equals(comment.getVid());
                    })
                    .toList();
        }

        List<UserCommentLike> dislikes = userCommentLikeMapper.selectList(queryWrapper);
        return dislikes.stream().map(UserCommentLike::getCommentId).toList();
    }

    @Override
    @Transactional
    public boolean deleteComment(Integer commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || Boolean.TRUE.equals(comment.getIsDeleted())) {
            throw new BusinessException(BusinessErrorCode.COMMENT_NOT_FOUND, "评论不存在或已被删除");
        }

        comment.setIsDeleted(Boolean.TRUE); // ✅ 软删除设为 true
        int result = commentMapper.updateById(comment);

        if (result > 0) {
            videoStatsService.decrementComment(comment.getVid());
        }

        return result > 0;
    }

    @Override
    public List<CommentTree> getCommentTreesByVid(Integer vid, Long offset, Integer type) {
        // 简化实现：返回根评论列表，每个根评论包含其子评论
        Page<Comment> page = new Page<>(offset / 20 + 1, 20);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid)
                .eq("root_id", BusinessConstants.COMMENT_ROOT_ID)
                .eq("is_deleted", Boolean.FALSE);
        
        // type: 1-按热度排序, 2-按时间排序
        if (type == 1) {
            queryWrapper.orderByDesc("love");
        } else {
            queryWrapper.orderByDesc("create_time");
        }

        List<Comment> rootComments = commentMapper.selectPage(page, queryWrapper).getRecords();
        
        // 转换为 CommentTree 并填充完整的用户信息和子评论
        return rootComments.stream().map(root -> {
            CommentTree tree = new CommentTree();
            tree.setId(root.getId());
            tree.setVid(root.getVid());
            tree.setUid(root.getUid());
            tree.setRootId(BusinessConstants.COMMENT_ROOT_ID); // 根评论的rootId为0
            tree.setParentId(BusinessConstants.COMMENT_ROOT_ID); // 根评论的parentId为0
            tree.setToUserId(root.getToUserId() != null ? root.getToUserId() : 0);
            tree.setContent(root.getContent());
            tree.setLove(root.getLove());
            tree.setBad(root.getBad() != null ? root.getBad() : 0);
            
            // 填充完整的用户信息对象
            User user = userService.getUserByIdSafe(root.getUid());
            java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
            if (user != null) {
                userInfo.put("uid", user.getUid());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatar_url", user.getAvatar());
                userInfo.put("auth", user.getAuth() != null ? user.getAuth() : 0);
                userInfo.put("vip", user.getVip() != null ? user.getVip() : 0);
                userInfo.put("exp", user.getExp() != null ? user.getExp() : 0);
            } else {
                userInfo.put("uid", root.getUid());
                userInfo.put("nickname", "未知用户");
                userInfo.put("avatar_url", BusinessConstants.DEFAULT_AVATAR_PATH);
                userInfo.put("auth", 0);
                userInfo.put("vip", 0);
                userInfo.put("exp", 0);
            }
            tree.setUser(userInfo);
            
            tree.setCreateTime(root.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            tree.setIsTop(Boolean.TRUE.equals(root.getIsTop()));
            
            // 查询子评论数量
            QueryWrapper<Comment> childQuery = new QueryWrapper<>();
            childQuery.eq("root_id", root.getId())
                    .eq("is_deleted", Boolean.FALSE);
            long childCount = commentMapper.selectCount(childQuery);
            tree.setCount((int) childCount);
            
            // 加载部分子评论（最多3条）
            if (childCount > 0) {
                childQuery.orderByAsc("create_time").last("LIMIT 3");
                List<Comment> childComments = commentMapper.selectList(childQuery);
                List<java.util.Map<String, Object>> replies = childComments.stream().map(child -> {
                    java.util.Map<String, Object> reply = new java.util.HashMap<>();
                    reply.put("id", child.getId());
                    reply.put("rootId", child.getRootId());
                    reply.put("parentId", child.getParentId());
                    reply.put("toUserId", child.getToUserId());
                    reply.put("content", child.getContent());
                    reply.put("love", child.getLove());
                    reply.put("bad", child.getBad() != null ? child.getBad() : 0);
                    reply.put("createTime", child.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    
                    // 子评论的用户信息
                    User childUser = userService.getUserByIdSafe(child.getUid());
                    java.util.Map<String, Object> childUserInfo = new java.util.HashMap<>();
                    if (childUser != null) {
                        childUserInfo.put("uid", childUser.getUid());
                        childUserInfo.put("nickname", childUser.getNickname());
                        childUserInfo.put("avatar_url", childUser.getAvatar());
                        childUserInfo.put("auth", childUser.getAuth() != null ? childUser.getAuth() : 0);
                        childUserInfo.put("vip", childUser.getVip() != null ? childUser.getVip() : 0);
                        childUserInfo.put("exp", childUser.getExp() != null ? childUser.getExp() : 0);
                    } else {
                        childUserInfo.put("uid", child.getUid());
                        childUserInfo.put("nickname", "未知用户");
                        childUserInfo.put("avatar_url", BusinessConstants.DEFAULT_AVATAR_PATH);
                        childUserInfo.put("auth", 0);
                        childUserInfo.put("vip", 0);
                        childUserInfo.put("exp", 0);
                    }
                    reply.put("user", childUserInfo);
                    
                    // 被回复用户的信息（toUser）
                    if (child.getToUserId() != null && child.getToUserId() != 0) {
                        User toUser = userService.getUserByIdSafe(child.getToUserId());
                        java.util.Map<String, Object> toUserInfo = new java.util.HashMap<>();
                        if (toUser != null) {
                            toUserInfo.put("uid", toUser.getUid());
                            toUserInfo.put("nickname", toUser.getNickname());
                            toUserInfo.put("avatar_url", toUser.getAvatar());
                            toUserInfo.put("auth", toUser.getAuth() != null ? toUser.getAuth() : 0);
                            toUserInfo.put("vip", toUser.getVip() != null ? toUser.getVip() : 0);
                            toUserInfo.put("exp", toUser.getExp() != null ? toUser.getExp() : 0);
                        } else {
                            toUserInfo.put("uid", child.getToUserId());
                            toUserInfo.put("nickname", "未知用户");
                            toUserInfo.put("avatar_url", BusinessConstants.DEFAULT_AVATAR_PATH);
                            toUserInfo.put("auth", 0);
                            toUserInfo.put("vip", 0);
                            toUserInfo.put("exp", 0);
                        }
                        reply.put("toUser", toUserInfo);
                    }
                    
                    return reply;
                }).toList();
                tree.setReplies(replies);
            } else {
                tree.setReplies(new java.util.ArrayList<>());
            }
            
            return tree;
        }).toList();
    }

    @Override
    public List<Integer> getUpLikedComments(Integer uid) {
        // 简化实现：返回空列表
        // 实际应该查询 user_comment_like 表
        return List.of();
    }

    @Override
    public java.util.Map<String, Object> buildCommentResponse(Comment comment) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();

        // 基本评论信息
        result.put("id", comment.getId());
        result.put("vid", comment.getVid());
        result.put("rootId", comment.getRootId() != null ? comment.getRootId() : 0);
        result.put("parentId", comment.getParentId() != null ? comment.getParentId() : 0);
        result.put("toUserId", comment.getToUserId() != null ? comment.getToUserId() : 0);
        result.put("content", comment.getContent());
        result.put("love", comment.getLove() != null ? comment.getLove() : 0);
        result.put("bad", comment.getBad() != null ? comment.getBad() : 0);
        result.put("createTime", comment.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("isTop", Boolean.TRUE.equals(comment.getIsTop()));
        result.put("count", 0); // 子评论数量，新评论默认为0
        result.put("replies", new java.util.ArrayList<>()); // 空的回复列表
        
        // 获取用户信息
        User user = userService.getUserByIdSafe(comment.getUid());
        java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
        if (user != null) {
            userInfo.put("uid", user.getUid());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar_url", user.getAvatar());
            userInfo.put("auth", user.getAuth() != null ? user.getAuth() : 0);
            userInfo.put("vip", user.getVip() != null ? user.getVip() : 0);
            userInfo.put("exp", user.getExp() != null ? user.getExp() : 0);
        } else {
            userInfo.put("uid", comment.getUid());
            userInfo.put("nickname", "未知用户");
            userInfo.put("avatar_url", BusinessConstants.DEFAULT_AVATAR_PATH);
            userInfo.put("auth", 0);
            userInfo.put("vip", 0);
            userInfo.put("exp", 0);
        }
        result.put("user", userInfo);
        
        // 如果是回复评论，添加被回复用户信息
        if (comment.getToUserId() != null && comment.getToUserId() != 0) {
            User toUser = userService.getUserByIdSafe(comment.getToUserId());
            java.util.Map<String, Object> toUserInfo = new java.util.HashMap<>();
            if (toUser != null) {
                toUserInfo.put("uid", toUser.getUid());
                toUserInfo.put("nickname", toUser.getNickname());
                toUserInfo.put("avatar_url", toUser.getAvatar());
                toUserInfo.put("auth", toUser.getAuth() != null ? toUser.getAuth() : 0);
                toUserInfo.put("vip", toUser.getVip() != null ? toUser.getVip() : 0);
                toUserInfo.put("exp", toUser.getExp() != null ? toUser.getExp() : 0);
            } else {
                toUserInfo.put("uid", comment.getToUserId());
                toUserInfo.put("nickname", "未知用户");
                toUserInfo.put("avatar_url", BusinessConstants.DEFAULT_AVATAR_PATH);
                toUserInfo.put("auth", 0);
                toUserInfo.put("vip", 0);
                toUserInfo.put("exp", 0);
            }
            result.put("toUser", toUserInfo);
        }
        
        return result;
    }

    private void handleCommentHierarchy(Comment comment) {
        if (comment.getParentId() == null) {
            comment.setParentId(BusinessConstants.COMMENT_ROOT_ID);
        }

        if (comment.getToUserId() == null) {
            comment.setToUserId(BusinessConstants.COMMENT_ROOT_ID);
        }

        if (Objects.equals(comment.getParentId(), BusinessConstants.COMMENT_ROOT_ID)) {
            comment.setRootId(BusinessConstants.COMMENT_ROOT_ID);
        } else {
            Comment parentComment = commentMapper.selectById(comment.getParentId());
            if (parentComment != null) {
                comment.setRootId(
                        Objects.equals(parentComment.getRootId(), BusinessConstants.COMMENT_ROOT_ID)
                                ? parentComment.getId()
                                : parentComment.getRootId()
                );
                comment.setToUserId(parentComment.getUid());
            } else {
                throw new BusinessException(BusinessErrorCode.COMMENT_PARENT_NOT_FOUND, "被回复的评论不存在");
            }
        }
    }
}