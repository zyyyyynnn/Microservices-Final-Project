package com.mallcloud.malladminbiz.service;

import com.mallcloud.malladminbiz.api.vo.DashboardVO;

/**
 * 后台看板服务
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
public interface AdminDashboardService {

    /**
     * 查询后台数据看板
     */
    DashboardVO getDashboard();
}
