package com.mallcloud.mallproduct.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mallcloud.mallproduct.domain.SpuAttr;
import com.mallcloud.mallproduct.mapper.SpuAttrMapper;
import com.mallcloud.mallproduct.service.SpuAttrService;
import org.springframework.stereotype.Service;
@Service
public class SpuAttrServiceImpl extends ServiceImpl<SpuAttrMapper, SpuAttr> implements SpuAttrService {}
