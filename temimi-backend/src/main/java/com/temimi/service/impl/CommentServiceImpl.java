package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.constant.BusinessConstants;
import com.temimi.exception.BusinessException;
import com.temimi.exception.BusinessErrorCode;
import com.temimi.mapper.CommentMapper;
import com.temimi.model.entity.Comment;
import com.temimi.model.entity.CommentTree;
import com.temimi.service.CommentService;
import com.temimi.service.VideoStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private VideoStatsService videoStatsService;

    @Override
    public List<Comment> getRootCommentsByVid(Integer vid, int pageNum, int pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid)
                .eq("root_id", BusinessConstants.COMMENT_ROOT_ID)
                .eq("is_deleted", BusinessConstants.FALSE)
                .orderByDesc("create_time");

        return commentMapper.selectPage(page, queryWrapper).getRecords();
    }

    @Override
    public CommentTree getCommentTreeByRootId(Integer rootId) {
        Comment rootComment = commentMapper.selectById(rootId);
        if (rootComment == null || rootComment.getIsDeleted()) {
            return null;
        }

        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("root_id", rootId)
                .eq("is_deleted", BusinessConstants.FALSE)
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
        commentTree.setIsTop(rootComment.getIsTop());
        commentTree.setChildren(null);

        return commentTree;
    }

    @Override
    @Transactional
    public boolean postComment(Comment comment, Integer uid) {
        comment.setUid(uid);
        comment.setCreateTime(LocalDateTime.now());
        comment.setLove(BusinessConstants.STATS_INITIAL_VALUE);
        comment.setBad(BusinessConstants.STATS_INITIAL_VALUE);
        comment.setIsTop(BusinessConstants.FALSE);
        comment.setIsDeleted(BusinessConstants.FALSE);

        handleCommentHierarchy(comment);

        int result = commentMapper.insert(comment);

        if (result > 0) {
            videoStatsService.incrementComment(comment.getVid());
        }
        
        return result > 0;
    }

    @Override
    @Transactional
    public boolean likeComment(Integer commentId, Integer uid) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getIsDeleted()) {
            throw new BusinessException(BusinessErrorCode.COMMENT_NOT_FOUND, "评论不存在或已被删除");
        }

        comment.setLove(comment.getLove() + 1);
        int result = commentMapper.updateById(comment);

        // TODO: 实现用户点赞记录表，避免重复点赞

        return result > 0;
    }

    @Override
    @Transactional
    public boolean unlikeComment(Integer commentId, Integer uid) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getIsDeleted()) {
            throw new BusinessException(BusinessErrorCode.COMMENT_NOT_FOUND, "评论不存在或已被删除");
        }

        int newLoveCount = Math.max(BusinessConstants.STATS_INITIAL_VALUE, comment.getLove() - 1);
        comment.setLove(newLoveCount);
        int result = commentMapper.updateById(comment);

        // TODO: 删除用户点赞记录

        return result > 0;
    }

    private void handleCommentHierarchy(Comment comment) {
        if (comment.getParentId() == BusinessConstants.COMMENT_ROOT_ID) {
            comment.setRootId(BusinessConstants.COMMENT_ROOT_ID);
        } else {
            Comment parentComment = commentMapper.selectById(comment.getParentId());
            if (parentComment != null) {
                comment.setRootId(parentComment.getRootId() == BusinessConstants.COMMENT_ROOT_ID ? 
                                 parentComment.getId() : parentComment.getRootId());
                comment.setToUserId(parentComment.getUid());
            } else {
                throw new BusinessException(BusinessErrorCode.COMMENT_PARENT_NOT_FOUND, "被回复的评论不存在");
            }
        }
    }
}