package com.mallcloud.mallinventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallinventory.api.dto.LockItemDTO;
import com.mallcloud.mallinventory.api.dto.LockStockDTO;
import com.mallcloud.mallinventory.api.dto.OrderNoDTO;
import com.mallcloud.mallinventory.api.vo.StockVO;
import com.mallcloud.mallinventory.domain.Stock;
import com.mallcloud.mallinventory.domain.StockLog;
import com.mallcloud.mallinventory.mapper.StockLogMapper;
import com.mallcloud.mallinventory.mapper.StockMapper;
import com.mallcloud.mallinventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final StockMapper stockMapper;
    private final StockLogMapper stockLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lock(LockStockDTO dto) {
        log.info("Locking stock for order: {}", dto.getOrderNo());
        QueryWrapper<StockLog> query = new QueryWrapper<>();
        query.eq("ref_no", dto.getOrderNo()).eq("type", "LOCK");
        if (stockLogMapper.selectCount(query) > 0) {
            log.info("Stock already locked for order: {}", dto.getOrderNo());
            return;
        }

        for (LockItemDTO item : dto.getItems()) {
            int rows = stockMapper.lockStock(item.getSkuId(), item.getQty());
            if (rows == 0) {
                log.error("Stock not enough for skuId: {}", item.getSkuId());
                throw new BizException(ErrorCode.STOCK_NOT_ENOUGH);
            }
            
            StockLog logRecord = new StockLog();
            logRecord.setSkuId(item.getSkuId());
            logRecord.setChangeQty(item.getQty());
            logRecord.setType("LOCK");
            logRecord.setRefNo(dto.getOrderNo());
            stockLogMapper.insert(logRecord);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deduct(OrderNoDTO dto) {
        log.info("Deducting stock for order: {}", dto.getOrderNo());
        QueryWrapper<StockLog> query = new QueryWrapper<>();
        query.eq("ref_no", dto.getOrderNo()).eq("type", "DEDUCT");
        if (stockLogMapper.selectCount(query) > 0) {
            log.info("Stock already deducted for order: {}", dto.getOrderNo());
            return;
        }

        QueryWrapper<StockLog> lockQuery = new QueryWrapper<>();
        lockQuery.eq("ref_no", dto.getOrderNo()).eq("type", "LOCK");
        List<StockLog> lockLogs = stockLogMapper.selectList(lockQuery);
        if (lockLogs.isEmpty()) {
            log.warn("No lock records found for order: {}", dto.getOrderNo());
            return;
        }

        for (StockLog lockLog : lockLogs) {
            int rows = stockMapper.deductStock(lockLog.getSkuId(), lockLog.getChangeQty());
            if (rows == 0) {
                log.error("Failed to deduct stock for skuId: {}", lockLog.getSkuId());
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }

            StockLog deductLog = new StockLog();
            deductLog.setSkuId(lockLog.getSkuId());
            deductLog.setChangeQty(lockLog.getChangeQty());
            deductLog.setType("DEDUCT");
            deductLog.setRefNo(dto.getOrderNo());
            stockLogMapper.insert(deductLog);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void release(OrderNoDTO dto) {
        log.info("Releasing stock for order: {}", dto.getOrderNo());
        QueryWrapper<StockLog> query = new QueryWrapper<>();
        query.eq("ref_no", dto.getOrderNo()).eq("type", "UNLOCK");
        if (stockLogMapper.selectCount(query) > 0) {
            log.info("Stock already released for order: {}", dto.getOrderNo());
            return;
        }
        
        QueryWrapper<StockLog> deductQuery = new QueryWrapper<>();
        deductQuery.eq("ref_no", dto.getOrderNo()).eq("type", "DEDUCT");
        if (stockLogMapper.selectCount(deductQuery) > 0) {
            log.warn("Stock already deducted, cannot release for order: {}", dto.getOrderNo());
            return;
        }

        QueryWrapper<StockLog> lockQuery = new QueryWrapper<>();
        lockQuery.eq("ref_no", dto.getOrderNo()).eq("type", "LOCK");
        List<StockLog> lockLogs = stockLogMapper.selectList(lockQuery);
        if (lockLogs.isEmpty()) {
            log.warn("No lock records found to release for order: {}", dto.getOrderNo());
            return;
        }

        for (StockLog lockLog : lockLogs) {
            int rows = stockMapper.releaseStock(lockLog.getSkuId(), lockLog.getChangeQty());
            if (rows == 0) {
                log.error("Failed to release stock for skuId: {}", lockLog.getSkuId());
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }

            StockLog releaseLog = new StockLog();
            releaseLog.setSkuId(lockLog.getSkuId());
            releaseLog.setChangeQty(lockLog.getChangeQty());
            releaseLog.setType("UNLOCK");
            releaseLog.setRefNo(dto.getOrderNo());
            stockLogMapper.insert(releaseLog);
        }
    }

    @Override
    public StockVO getStock(Long skuId) {
        Stock stock = stockMapper.selectOne(new QueryWrapper<Stock>().eq("sku_id", skuId));
        if (stock == null) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "库存不存在");
        }
        StockVO vo = new StockVO();
        vo.setSkuId(stock.getSkuId());
        vo.setTotal(stock.getTotal());
        vo.setLocked(stock.getLocked());
        vo.setAvailable(stock.getAvailable());
        return vo;
    }

    @Override
    public int reconcileStock() {
        QueryWrapper<Stock> query = new QueryWrapper<>();
        query.lt("available", 0)
                .or()
                .lt("locked", 0)
                .or()
                .apply("total <> available + locked");
        int abnormalCount = Math.toIntExact(stockMapper.selectCount(query));
        if (abnormalCount > 0) {
            log.warn("库存对账发现异常记录 count={}", abnormalCount);
        } else {
            log.info("库存对账正常");
        }
        return abnormalCount;
    }
}
