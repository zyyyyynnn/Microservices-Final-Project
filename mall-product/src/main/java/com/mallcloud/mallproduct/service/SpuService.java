package com.mallcloud.mallproduct.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mallcloud.mallcommon.response.PageData;
import com.mallcloud.mallproduct.api.dto.AdminProductQueryDTO;
import com.mallcloud.mallproduct.api.vo.AdminProductVO;
import com.mallcloud.mallproduct.api.vo.ProductStatsVO;
import com.mallcloud.mallproduct.domain.Spu;
import com.mallcloud.mallproduct.api.vo.SpuDetailVO;
import com.mallcloud.mallproduct.api.dto.ProductSaveDTO;
import java.util.List;

public interface SpuService extends IService<Spu> {
    SpuDetailVO getSpuDetail(Long spuId);
    void saveProduct(ProductSaveDTO dto);
    void updateProduct(Long id, ProductSaveDTO dto);
    void deleteProduct(Long id);
    void updateStatus(Long id, Integer status);
    PageData<AdminProductVO> listAdminProducts(AdminProductQueryDTO query);
    ProductStatsVO getProductStats();
    List<Long> listOnSaleSpuIds();
}
