package com.temimi.model.entity;

import com.temimi.constant.CommandType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket消息命令类
 * 统一WebSocket消息格式，通过code字段标识消息类型，content字段承载数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Command {
    
    /**
     * 命令类型代码
     */
    private Integer code;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 使用CommandType枚举创建Command
     */
    public Command(CommandType commandType, String content) {
        this.code = commandType.getCode();
        this.content = content;
    }
    
    /**
     * 获取CommandType枚举
     */
    public CommandType getCommandType() {
        return CommandType.match(this.code);
    }
    
    /**
     * 判断是否为指定的命令类型
     */
    public boolean isCommandType(CommandType commandType) {
        return commandType.getCode().equals(this.code);
    }
    
    /**
     * 创建成功响应命令
     */
    public static Command success(CommandType commandType, String content) {
        return new Command(commandType, content);
    }
    
    /**
     * 创建错误响应命令
     */
    public static Command error(String errorMessage) {
        return new Command(CommandType.ERROR, errorMessage);
    }
    
    /**
     * 创建心跳命令
     */
    public static Command heartbeat() {
        return new Command(CommandType.HEARTBEAT, "ping");
    }
    
    /**
     * 创建心跳响应命令
     */
    public static Command heartbeatResponse() {
        return new Command(CommandType.HEARTBEAT, "pong");
    }
}