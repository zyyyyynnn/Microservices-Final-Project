package com.mallcloud.mallproduct.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mallcloud.mallproduct.domain.SpuImg;
import com.mallcloud.mallproduct.mapper.SpuImgMapper;
import com.mallcloud.mallproduct.service.SpuImgService;
import org.springframework.stereotype.Service;
@Service
public class SpuImgServiceImpl extends ServiceImpl<SpuImgMapper, SpuImg> implements SpuImgService {}
