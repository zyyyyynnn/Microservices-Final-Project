package com.mallcloud.mallproduct.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mallcloud.mallproduct.domain.Category;
import com.mallcloud.mallproduct.api.vo.CategoryTreeVO;
import java.util.List;
public interface CategoryService extends IService<Category> {
    List<CategoryTreeVO> getTree();
}
