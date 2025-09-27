// 新建文件: src/main/java/com/shiyou/model/dto/DanmuMessageDTO.java
package com.temimi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DanmuMessageDTO {
    private String content;
    private Double time;
    private Integer mode;
    private String color;
}