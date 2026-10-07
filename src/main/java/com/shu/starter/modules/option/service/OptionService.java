package com.shu.starter.modules.option.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shu.starter.modules.option.entity.OptionEntity;
import com.shu.starter.modules.option.mapper.OptionMapper;
import org.springframework.stereotype.Service;

@Service
public class OptionService {
  private final OptionMapper optionMapper;

  public OptionService(OptionMapper optionMapper) {
    this.optionMapper = optionMapper;
  }

  public String getOption(String type, String name) {
    OptionEntity entity = find(type, name);
    return entity == null ? null : entity.getValue();
  }

  public void setOption(String type, String name, String value) {
    OptionEntity entity = find(type, name);
    if (entity == null) {
      entity = new OptionEntity();
      entity.setType(type);
      entity.setName(name);
      entity.setValue(value);
      optionMapper.insert(entity);
    } else {
      entity.setValue(value);
      optionMapper.updateById(entity);
    }
  }

  private OptionEntity find(String type, String name) {
    return optionMapper.selectOne(
        new LambdaQueryWrapper<OptionEntity>()
            .eq(OptionEntity::getType, type)
            .eq(OptionEntity::getName, name));
  }
}
