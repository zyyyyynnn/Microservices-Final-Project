package com.mallcloud.mallproduct.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mallcloud.mallproduct.domain.Sku;
import com.mallcloud.mallproduct.mapper.SkuMapper;
import com.mallcloud.mallproduct.service.SkuService;
import org.springframework.stereotype.Service;
@Service
public class SkuServiceImpl extends ServiceImpl<SkuMapper, Sku> implements SkuService {}
