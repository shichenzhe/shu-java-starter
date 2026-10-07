package com.shu.starter.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shu.starter.modules.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {}
