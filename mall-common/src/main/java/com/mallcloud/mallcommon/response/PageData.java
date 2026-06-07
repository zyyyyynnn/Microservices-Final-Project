package com.mallcloud.mallcommon.response;

import com.mallcloud.mallcommon.enums.ErrorCode;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页数据
 *
 * @author zhangsan
 * @since 2026-03-01
 */
@Data
public class PageData<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;
    private boolean hasNext;

    public PageData() {
        this.list = Collections.emptyList();
    }

    public PageData(List<T> list, long total, int pageNum, int pageSize) {
        this.list = list == null ? Collections.emptyList() : list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.hasNext = (long) pageNum * pageSize < total;
    }

    public static <T> PageData<T> of(List<T> list, long total, int pageNum, int pageSize) {
        return new PageData<>(list, total, pageNum, pageSize);
    }

    public static <T> PageData<T> empty(int pageNum, int pageSize) {
        return new PageData<>(Collections.emptyList(), 0, pageNum, pageSize);
    }
}
