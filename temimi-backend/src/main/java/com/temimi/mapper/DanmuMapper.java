package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.Danmu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 弹幕表 Mapper 接口
 */
@Mapper
public interface DanmuMapper extends BaseMapper<Danmu> {
    // 基础CRUD已由BaseMapper提供
}