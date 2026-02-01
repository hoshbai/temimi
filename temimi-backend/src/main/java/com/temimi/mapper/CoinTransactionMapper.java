package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.CoinTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CoinTransactionMapper extends BaseMapper<CoinTransaction> {
}
