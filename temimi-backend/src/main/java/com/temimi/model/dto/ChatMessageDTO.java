// 新建文件: src/main/java/com/shiyou/model/dto/ChatMessageDTO.java
package com.temimi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private Integer toUid;
    private String content;
}