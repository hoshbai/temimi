package com.temimi.service.impl;

import com.temimi.model.entity.Message;
import com.temimi.service.MessageService;
import com.temimi.service.SystemNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 系统通知服务实现类
 */
@Slf4j
@Service
public class SystemNotificationServiceImpl implements SystemNotificationService {

    @Autowired
    private MessageService messageService;

    // 系统用户ID，用于发送系统消息
    private static final Integer SYSTEM_USER_ID = 0;

    @Override
    public void sendDailyLoginRewardNotification(Integer uid, int coinAmount) {
        try {
            Message message = new Message();
            message.setFromUid(SYSTEM_USER_ID);
            message.setToUid(uid);
            message.setType("system");
            message.setContent("恭喜您获得每日登录奖励 " + coinAmount + " 个硬币！每天登录都有奖励哦~");
            message.setTargetType("coin");
            message.setTargetId(null);
            message.setTargetContent("每日登录奖励");
            message.setIsRead(false);
            message.setCreateTime(LocalDateTime.now());

            messageService.createMessage(message);
            log.info("发送每日登录奖励通知给用户 {}", uid);
        } catch (Exception e) {
            log.error("发送每日登录奖励通知失败，用户: {}", uid, e);
        }
    }

    @Override
    public void sendCoinRewardNotification(Integer upUid, Integer fromUid, Integer vid, String videoTitle, double coinReward) {
        try {
            Message message = new Message();
            message.setFromUid(SYSTEM_USER_ID);
            message.setToUid(upUid);
            message.setType("system");
            message.setContent(String.format("您的视频《%s》收到投币，获得 %.1f 个硬币奖励！", videoTitle, coinReward));
            message.setTargetType("video");
            message.setTargetId(vid);
            message.setTargetContent(videoTitle);
            message.setIsRead(false);
            message.setCreateTime(LocalDateTime.now());

            messageService.createMessage(message);
            log.info("发送投币奖励通知给UP主 {}，视频: {}", upUid, vid);
        } catch (Exception e) {
            log.error("发送投币奖励通知失败，UP主: {}，视频: {}", upUid, vid, e);
        }
    }

    @Override
    public void sendCollectMilestoneNotification(Integer uid, Integer vid, String videoTitle, int milestone, int coinReward) {
        try {
            Message message = new Message();
            message.setFromUid(SYSTEM_USER_ID);
            message.setToUid(uid);
            message.setType("system");
            message.setContent(String.format("恭喜！您的视频《%s》收藏数达到 %d，获得 %d 个硬币奖励！", videoTitle, milestone, coinReward));
            message.setTargetType("video");
            message.setTargetId(vid);
            message.setTargetContent(videoTitle);
            message.setIsRead(false);
            message.setCreateTime(LocalDateTime.now());

            messageService.createMessage(message);
            log.info("发送收藏里程碑通知给用户 {}，视频: {}，里程碑: {}", uid, vid, milestone);
        } catch (Exception e) {
            log.error("发送收藏里程碑通知失败，用户: {}，视频: {}", uid, vid, e);
        }
    }

    @Override
    public void sendVideoApprovedNotification(Integer uid, Integer vid, String videoTitle) {
        try {
            Message message = new Message();
            message.setFromUid(SYSTEM_USER_ID);
            message.setToUid(uid);
            message.setType("system");
            message.setContent(String.format("您的视频《%s》已通过审核，现在可以被其他用户看到啦！", videoTitle));
            message.setTargetType("video");
            message.setTargetId(vid);
            message.setTargetContent(videoTitle);
            message.setIsRead(false);
            message.setCreateTime(LocalDateTime.now());

            messageService.createMessage(message);
            log.info("发送视频审核通过通知给用户 {}，视频: {}", uid, vid);
        } catch (Exception e) {
            log.error("发送视频审核通过通知失败，用户: {}，视频: {}", uid, vid, e);
        }
    }

    @Override
    public void sendVideoRejectedNotification(Integer uid, Integer vid, String videoTitle, String reason) {
        try {
            Message message = new Message();
            message.setFromUid(SYSTEM_USER_ID);
            message.setToUid(uid);
            message.setType("system");
            String content = String.format("很抱歉，您的视频《%s》未通过审核", videoTitle);
            if (reason != null && !reason.trim().isEmpty()) {
                content += "，原因：" + reason;
            }
            content += "。请修改后重新投稿。";
            message.setContent(content);
            message.setTargetType("video");
            message.setTargetId(vid);
            message.setTargetContent(videoTitle);
            message.setIsRead(false);
            message.setCreateTime(LocalDateTime.now());

            messageService.createMessage(message);
            log.info("发送视频审核拒绝通知给用户 {}，视频: {}", uid, vid);
        } catch (Exception e) {
            log.error("发送视频审核拒绝通知失败，用户: {}，视频: {}", uid, vid, e);
        }
    }

    @Override
    public void sendVideoDeletedNotification(Integer uid, Integer vid, String videoTitle) {
        try {
            Message message = new Message();
            message.setFromUid(SYSTEM_USER_ID);
            message.setToUid(uid);
            message.setType("system");
            message.setContent(String.format("您的视频《%s》因违规已被删除。如有疑问，请联系客服。", videoTitle));
            message.setTargetType("video");
            message.setTargetId(vid);
            message.setTargetContent(videoTitle);
            message.setIsRead(false);
            message.setCreateTime(LocalDateTime.now());

            messageService.createMessage(message);
            log.info("发送视频删除通知给用户 {}，视频: {}", uid, vid);
        } catch (Exception e) {
            log.error("发送视频删除通知失败，用户: {}，视频: {}", uid, vid, e);
        }
    }
}
