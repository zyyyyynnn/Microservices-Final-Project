package com.mallcloud.mallproduct.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mallcloud.mallproduct.domain.Category;
import com.mallcloud.mallproduct.mapper.CategoryMapper;
import com.mallcloud.mallproduct.service.CategoryService;
import com.mallcloud.mallproduct.api.vo.CategoryTreeVO;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Override
    public List<CategoryTreeVO> getTree() {
        List<Category> all = query().select("id", "parent_id", "name", "level", "icon", "sort", "status").list();
        return all.stream()
            .filter(c -> c.getParentId() != null && c.getParentId() == 0L)
            .map(c -> {
                CategoryTreeVO vo = new CategoryTreeVO();
                BeanUtils.copyProperties(c, vo);
                vo.setChildren(getChildren(c.getId(), all));
                return vo;
            })
            .collect(Collectors.toList());
    }

    private List<CategoryTreeVO> getChildren(Long parentId, List<Category> all) {
        return all.stream()
            .filter(c -> c.getParentId() != null && c.getParentId().equals(parentId))
            .map(c -> {
                CategoryTreeVO vo = new CategoryTreeVO();
                BeanUtils.copyProperties(c, vo);
                vo.setChildren(getChildren(c.getId(), all));
                return vo;
            })
            .collect(Collectors.toList());
    }
}
