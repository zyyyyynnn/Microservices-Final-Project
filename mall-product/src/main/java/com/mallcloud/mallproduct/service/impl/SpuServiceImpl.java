package com.mallcloud.mallproduct.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mallcloud.mallcommon.response.PageData;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallproduct.api.dto.AdminProductQueryDTO;
import com.mallcloud.mallproduct.api.dto.ProductSaveDTO;
import com.mallcloud.mallproduct.api.vo.AdminProductVO;
import com.mallcloud.mallproduct.api.vo.ProductStatsVO;
import com.mallcloud.mallproduct.api.vo.SkuVO;
import com.mallcloud.mallproduct.api.vo.SpuAttrVO;
import com.mallcloud.mallproduct.api.vo.SpuDetailVO;
import com.mallcloud.mallproduct.api.vo.TopProductVO;
import com.mallcloud.mallproduct.client.InventoryClient;
import com.mallcloud.mallproduct.domain.Sku;
import com.mallcloud.mallproduct.domain.Spu;
import com.mallcloud.mallproduct.domain.SpuAttr;
import com.mallcloud.mallproduct.mapper.SpuMapper;
import com.mallcloud.mallproduct.service.SkuService;
import com.mallcloud.mallproduct.service.SpuAttrService;
import com.mallcloud.mallproduct.service.SpuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpuServiceImpl extends ServiceImpl<SpuMapper, Spu> implements SpuService {

    private final SkuService skuService;
    private final SpuAttrService spuAttrService;
    private final InventoryClient inventoryClient;
    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public SpuDetailVO getSpuDetail(Long spuId) {
        Spu spu = query().select("id", "name", "description", "main_image", "category_id", "brand", "merchant_id", "status", "sales", "view_count", "gmt_create", "gmt_modified")
                .eq("id", spuId).one();
        if (spu == null) {
            throw new BizException(30100, "商品不存在");
        }

        SpuDetailVO vo = new SpuDetailVO();
        vo.setSpuId(spu.getId());
        vo.setName(spu.getName());
        vo.setDescription(spu.getDescription());
        vo.setMainImage(spu.getMainImage());
        vo.setCategoryId(spu.getCategoryId());
        vo.setBrand(spu.getBrand());
        vo.setStatus(spu.getStatus());
        vo.setSales(spu.getSales());

        List<Sku> skus = skuService.query().select("id", "spu_id", "spec_json", "price", "original_price", "image", "weight", "barcode", "status")
                .eq("spu_id", spuId).list();
        List<SkuVO> skuVOs = skus.stream().map(sku -> {
            SkuVO skuVO = new SkuVO();
            skuVO.setSkuId(sku.getId());
            skuVO.setSpec(sku.getSpecJson());
            skuVO.setPrice(sku.getPrice());
            skuVO.setImage(sku.getImage());
            
            try {
                Result<Integer> stockResult = inventoryClient.getStock(sku.getId());
                if (stockResult != null && stockResult.getData() != null) {
                    skuVO.setStock(stockResult.getData());
                } else {
                    skuVO.setStock(0);
                }
            } catch (Exception e) {
                log.error("Failed to get stock for sku: {}", sku.getId(), e);
                skuVO.setStock(0);
            }
            return skuVO;
        }).collect(Collectors.toList());
        vo.setSkus(skuVOs);

        List<SpuAttr> attrs = spuAttrService.query().select("id", "spu_id", "attr_name", "attr_value")
                .eq("spu_id", spuId).list();
        List<SpuAttrVO> attrVOs = attrs.stream().map(attr -> {
            SpuAttrVO attrVO = new SpuAttrVO();
            attrVO.setName(attr.getAttrName());
            attrVO.setValue(attr.getAttrValue());
            return attrVO;
        }).collect(Collectors.toList());
        vo.setAttrs(attrVOs);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProduct(ProductSaveDTO dto) {
        Spu spu = new Spu();
        BeanUtils.copyProperties(dto, spu);
        spu.setStatus(0);
        save(spu);

        if (dto.getSkus() != null && !dto.getSkus().isEmpty()) {
            List<Sku> skus = dto.getSkus().stream().map(skuDTO -> {
                Sku sku = new Sku();
                BeanUtils.copyProperties(skuDTO, sku);
                sku.setSpuId(spu.getId());
                sku.setStatus(1);
                return sku;
            }).collect(Collectors.toList());
            skuService.saveBatch(skus);
        }

        if (dto.getAttrs() != null && !dto.getAttrs().isEmpty()) {
            List<SpuAttr> attrs = dto.getAttrs().stream().map(attrDTO -> {
                SpuAttr attr = new SpuAttr();
                BeanUtils.copyProperties(attrDTO, attr);
                attr.setSpuId(spu.getId());
                return attr;
            }).collect(Collectors.toList());
            spuAttrService.saveBatch(attrs);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long id, ProductSaveDTO dto) {
        Spu spu = getById(id);
        if (spu == null) {
            throw new BizException(30100, "商品不存在");
        }
        BeanUtils.copyProperties(dto, spu);
        updateById(spu);

        skuService.remove(Wrappers.<Sku>lambdaQuery().eq(Sku::getSpuId, id));
        if (dto.getSkus() != null && !dto.getSkus().isEmpty()) {
            List<Sku> skus = dto.getSkus().stream().map(skuDTO -> {
                Sku sku = new Sku();
                BeanUtils.copyProperties(skuDTO, sku);
                sku.setSpuId(id);
                sku.setStatus(1);
                return sku;
            }).collect(Collectors.toList());
            skuService.saveBatch(skus);
        }

        spuAttrService.remove(Wrappers.<SpuAttr>lambdaQuery().eq(SpuAttr::getSpuId, id));
        if (dto.getAttrs() != null && !dto.getAttrs().isEmpty()) {
            List<SpuAttr> attrs = dto.getAttrs().stream().map(attrDTO -> {
                SpuAttr attr = new SpuAttr();
                BeanUtils.copyProperties(attrDTO, attr);
                attr.setSpuId(id);
                return attr;
            }).collect(Collectors.toList());
            spuAttrService.saveBatch(attrs);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        removeById(id);
        skuService.remove(Wrappers.<Sku>lambdaQuery().eq(Sku::getSpuId, id));
        spuAttrService.remove(Wrappers.<SpuAttr>lambdaQuery().eq(SpuAttr::getSpuId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Spu spu = getById(id);
        if (spu == null) {
            throw new BizException(30100, "商品不存在");
        }
        spu.setStatus(status);
        updateById(spu);

        try {
            rocketMQTemplate.convertAndSend("ES_SYNC", objectMapper.writeValueAsString(Map.of(
                    "spuId", id,
                    "status", status
            )));
        } catch (Exception e) {
            log.error("Failed to send ES_SYNC message for spuId: {}", id, e);
        }
    }

    @Override
    public PageData<AdminProductVO> listAdminProducts(AdminProductQueryDTO query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : query.getPageSize();
        LambdaQueryWrapper<Spu> wrapper = Wrappers.<Spu>lambdaQuery()
                .select(Spu::getId, Spu::getName, Spu::getMainImage, Spu::getCategoryId,
                        Spu::getBrand, Spu::getStatus, Spu::getSales)
                .like(query.getKeyword() != null && !query.getKeyword().isBlank(), Spu::getName, query.getKeyword())
                .eq(query.getStatus() != null, Spu::getStatus, query.getStatus())
                .orderByDesc(Spu::getGmtModified);
        Page<Spu> page = page(new Page<>(pageNum, pageSize), wrapper);
        List<AdminProductVO> list = page.getRecords().stream().map(spu -> {
            AdminProductVO vo = new AdminProductVO();
            vo.setSpuId(spu.getId());
            vo.setName(spu.getName());
            vo.setMainImage(spu.getMainImage());
            vo.setCategoryId(spu.getCategoryId());
            vo.setBrand(spu.getBrand());
            vo.setStatus(spu.getStatus());
            vo.setSales(spu.getSales());
            return vo;
        }).toList();
        return PageData.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public ProductStatsVO getProductStats() {
        ProductStatsVO vo = new ProductStatsVO();
        vo.setTotalProducts(count());
        List<TopProductVO> topProducts = query()
                .select("id", "name", "sales")
                .orderByDesc("sales")
                .last("LIMIT 5")
                .list()
                .stream()
                .map(spu -> {
                    TopProductVO item = new TopProductVO();
                    item.setSpuId(spu.getId());
                    item.setName(spu.getName());
                    item.setSales(spu.getSales());
                    return item;
                })
                .toList();
        vo.setTopProducts(topProducts);
        return vo;
    }

    @Override
    public List<Long> listOnSaleSpuIds() {
        return query()
                .select("id")
                .eq("status", 1)
                .list()
                .stream()
                .map(Spu::getId)
                .toList();
    }
}
