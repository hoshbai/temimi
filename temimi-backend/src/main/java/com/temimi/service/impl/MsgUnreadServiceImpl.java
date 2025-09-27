package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.constant.BusinessConstants;
import com.temimi.exception.BusinessException;
import com.temimi.exception.BusinessErrorCode;
import com.temimi.mapper.MsgUnreadMapper;
import com.temimi.model.entity.MsgUnread;
import com.temimi.service.MsgUnreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MsgUnreadServiceImpl extends ServiceImpl<MsgUnreadMapper, MsgUnread> implements MsgUnreadService {

    @Autowired
    private MsgUnreadMapper msgUnreadMapper;

    @Override
    public MsgUnread getUnreadCountByUid(Integer uid) {
        return msgUnreadMapper.selectById(uid);
    }

    @Override
    @Transactional
    public boolean clearUnreadCount(Integer uid, String category) {
        UpdateWrapper<MsgUnread> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid);

        switch (category) {
            case BusinessConstants.MSG_TYPE_REPLY:
                updateWrapper.set("reply", BusinessConstants.STATS_INITIAL_VALUE);
                break;
            case BusinessConstants.MSG_TYPE_AT:
                updateWrapper.set("at", BusinessConstants.STATS_INITIAL_VALUE);
                break;
            case BusinessConstants.MSG_TYPE_LOVE:
                updateWrapper.set("love", BusinessConstants.STATS_INITIAL_VALUE);
                break;
            case BusinessConstants.MSG_TYPE_SYSTEM:
                updateWrapper.set("system", BusinessConstants.STATS_INITIAL_VALUE);
                break;
            case BusinessConstants.MSG_TYPE_WHISPER:
                updateWrapper.set("whisper", BusinessConstants.STATS_INITIAL_VALUE);
                break;
            case BusinessConstants.MSG_TYPE_DYNAMIC:
                updateWrapper.set("dynamic", BusinessConstants.STATS_INITIAL_VALUE);
                break;
            default:
                throw new BusinessException(BusinessErrorCode.SYSTEM_ERROR, "不支持的消息类型: " + category);
        }

        int result = msgUnreadMapper.update(null, updateWrapper);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean incrementUnreadCount(Integer uid, String category) {
        UpdateWrapper<MsgUnread> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid);

        switch (category) {
            case BusinessConstants.MSG_TYPE_REPLY:
                updateWrapper.setSql("reply = reply + 1");
                break;
            case BusinessConstants.MSG_TYPE_AT:
                updateWrapper.setSql("at = at + 1");
                break;
            case BusinessConstants.MSG_TYPE_LOVE:
                updateWrapper.setSql("love = love + 1");
                break;
            case BusinessConstants.MSG_TYPE_SYSTEM:
                updateWrapper.setSql("system = system + 1");
                break;
            case BusinessConstants.MSG_TYPE_WHISPER:
                updateWrapper.setSql("whisper = whisper + 1");
                break;
            case BusinessConstants.MSG_TYPE_DYNAMIC:
                updateWrapper.setSql("dynamic = dynamic + 1");
                break;
            default:
                throw new BusinessException(BusinessErrorCode.SYSTEM_ERROR, "不支持的消息类型: " + category);
        }

        int result = msgUnreadMapper.update(null, updateWrapper);
        return result > 0;
    }
}