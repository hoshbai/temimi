// 新建文件: src/main/java/com/shiyou/model/dto/DanmuMessageDTO.java
package com.temimi.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)  // ✅ 忽略未知字段
public class DanmuMessageDTO {
    private String content;
    
    @JsonAlias("timePoint")  // ✅ 支持前端的 timePoint 字段名
    private Double time;
    
    private Integer mode;
    private String color;
    
    @JsonAlias("fontsize")  // ✅ 支持前端的 fontsize 字段名
    private Integer fontsize;
}