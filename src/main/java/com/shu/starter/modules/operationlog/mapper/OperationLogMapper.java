package com.shu.starter.modules.operationlog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shu.starter.modules.operationlog.entity.OperationLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogEntity> {}
