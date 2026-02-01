package com.temimi.service;

/**
 * 系统通知服务接口
 * 用于发送各类系统通知消息
 */
public interface SystemNotificationService {

    /**
     * 发送每日登录奖励通知
     * @param uid 用户ID
     * @param coinAmount 获得的硬币数量
     */
    void sendDailyLoginRewardNotification(Integer uid, int coinAmount);

    /**
     * 发送投币奖励通知（UP主收到投币）
     * @param upUid UP主用户ID
     * @param fromUid 投币用户ID
     * @param vid 视频ID
     * @param videoTitle 视频标题
     * @param coinReward 获得的硬币奖励
     */
    void sendCoinRewardNotification(Integer upUid, Integer fromUid, Integer vid, String videoTitle, double coinReward);

    /**
     * 发送收藏里程碑奖励通知
     * @param uid UP主用户ID
     * @param vid 视频ID
     * @param videoTitle 视频标题
     * @param milestone 达到的里程碑数
     * @param coinReward 获得的硬币奖励
     */
    void sendCollectMilestoneNotification(Integer uid, Integer vid, String videoTitle, int milestone, int coinReward);

    /**
     * 发送视频审核通过通知
     * @param uid 视频作者ID
     * @param vid 视频ID
     * @param videoTitle 视频标题
     */
    void sendVideoApprovedNotification(Integer uid, Integer vid, String videoTitle);

    /**
     * 发送视频审核拒绝通知
     * @param uid 视频作者ID
     * @param vid 视频ID
     * @param videoTitle 视频标题
     * @param reason 拒绝原因（可选）
     */
    void sendVideoRejectedNotification(Integer uid, Integer vid, String videoTitle, String reason);

    /**
     * 发送视频被删除通知
     * @param uid 视频作者ID
     * @param vid 视频ID
     * @param videoTitle 视频标题
     */
    void sendVideoDeletedNotification(Integer uid, Integer vid, String videoTitle);
}
