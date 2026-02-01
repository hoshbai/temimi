package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.temimi.mapper.MsgUnreadMapper;
import com.temimi.model.entity.MsgUnread;
import com.temimi.service.MsgUnreadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息未读数服务实现类
 */
@Slf4j
@Service
public class MsgUnreadServiceImpl implements MsgUnreadService {

    @Autowired
    private MsgUnreadMapper msgUnreadMapper;

    @Override
    public MsgUnread getUnreadCountByUid(Integer uid) {
        MsgUnread msgUnread = msgUnreadMapper.selectById(uid);
        if (msgUnread == null) {
            // 如果不存在，初始化
            initUnreadRecord(uid);
            msgUnread = msgUnreadMapper.selectById(uid);
        }
        return msgUnread;
    }

    @Override
    public boolean clearUnreadCount(Integer uid, String category) {
        LambdaUpdateWrapper<MsgUnread> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MsgUnread::getUid, uid);

        switch (category) {
            case "reply":
                updateWrapper.set(MsgUnread::getReply, 0);
                break;
            case "at":
                updateWrapper.set(MsgUnread::getAt, 0);
                break;
            case "love":
                updateWrapper.set(MsgUnread::getLove, 0);
                break;
            case "system":
                updateWrapper.set(MsgUnread::getSystem, 0);
                break;
            case "whisper":
                updateWrapper.set(MsgUnread::getWhisper, 0);
                break;
            case "dynamic":
                updateWrapper.set(MsgUnread::getDynamic, 0);
                break;
            default:
                log.warn("未知的未读消息类型: {}", category);
                return false;
        }

        return msgUnreadMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean incrementUnreadCount(Integer uid, String category) {
        MsgUnread msgUnread = getUnreadCountByUid(uid);

        switch (category) {
            case "reply":
                msgUnread.setReply(msgUnread.getReply() + 1);
                break;
            case "at":
                msgUnread.setAt(msgUnread.getAt() + 1);
                break;
            case "love":
                msgUnread.setLove(msgUnread.getLove() + 1);
                break;
            case "system":
                msgUnread.setSystem(msgUnread.getSystem() + 1);
                break;
            case "whisper":
                msgUnread.setWhisper(msgUnread.getWhisper() + 1);
                break;
            case "dynamic":
                msgUnread.setDynamic(msgUnread.getDynamic() + 1);
                break;
            default:
                log.warn("未知的未读消息类型: {}", category);
                return false;
        }

        return msgUnreadMapper.updateById(msgUnread) > 0;
    }

    @Override
    public boolean decrementUnreadCount(Integer uid, String category, int count) {
        if (count <= 0) {
            return true;
        }
        
        MsgUnread msgUnread = getUnreadCountByUid(uid);
        int currentCount = 0;

        switch (category) {
            case "reply":
                currentCount = msgUnread.getReply();
                msgUnread.setReply(Math.max(0, currentCount - count));
                break;
            case "at":
                currentCount = msgUnread.getAt();
                msgUnread.setAt(Math.max(0, currentCount - count));
                break;
            case "love":
                currentCount = msgUnread.getLove();
                msgUnread.setLove(Math.max(0, currentCount - count));
                break;
            case "system":
                currentCount = msgUnread.getSystem();
                msgUnread.setSystem(Math.max(0, currentCount - count));
                break;
            case "whisper":
                currentCount = msgUnread.getWhisper();
                msgUnread.setWhisper(Math.max(0, currentCount - count));
                break;
            case "dynamic":
                currentCount = msgUnread.getDynamic();
                msgUnread.setDynamic(Math.max(0, currentCount - count));
                break;
            default:
                log.warn("未知的未读消息类型: {}", category);
                return false;
        }

        return msgUnreadMapper.updateById(msgUnread) > 0;
    }

    @Override
    public void initUnreadRecord(Integer uid) {
        MsgUnread msgUnread = new MsgUnread();
        msgUnread.setUid(uid);
        msgUnread.setReply(0);
        msgUnread.setAt(0);
        msgUnread.setLove(0);
        msgUnread.setSystem(0);
        msgUnread.setWhisper(0);
        msgUnread.setDynamic(0);
        msgUnreadMapper.insert(msgUnread);
    }
}
