package com.mallcloud.malluser.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallcommon.util.UserContext;
import com.mallcloud.malluser.api.dto.AddressDTO;
import com.mallcloud.malluser.api.vo.AddressVO;
import com.mallcloud.malluser.domain.entity.Address;
import com.mallcloud.malluser.mapper.AddressMapper;
import com.mallcloud.malluser.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public List<AddressVO> listCurrentUserAddresses() {
        Long userId = UserContext.requireUserId();
        List<Address> addresses = list(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getGmtCreate));
        return addresses.stream().map(addr -> {
            AddressVO vo = new AddressVO();
            BeanUtils.copyProperties(addr, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public AddressVO getInternalAddress(Long userId, Long addressId) {
        if (userId == null || addressId == null) {
            return null;
        }
        Address address = getOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getId, addressId));
        if (address == null) {
            return null;
        }
        AddressVO vo = new AddressVO();
        BeanUtils.copyProperties(address, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAddress(AddressDTO dto) {
        Long userId = UserContext.requireUserId();
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            clearDefaultAddress(userId);
        }
        
        Address address = new Address();
        BeanUtils.copyProperties(dto, address);
        address.setUserId(userId);
        address.setIsDefault(Boolean.TRUE.equals(dto.getIsDefault()) ? 1 : 0);
        address.setGmtCreate(LocalDateTime.now());
        save(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long id, AddressDTO dto) {
        Long userId = UserContext.requireUserId();
        Address address = getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BizException(10001, "地址不存在或无权限");
        }

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            clearDefaultAddress(userId);
        }

        BeanUtils.copyProperties(dto, address);
        address.setIsDefault(Boolean.TRUE.equals(dto.getIsDefault()) ? 1 : 0);
        updateById(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long id) {
        Long userId = UserContext.requireUserId();
        Address address = getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BizException(10001, "地址不存在或无权限");
        }
        removeById(id);
    }

    private void clearDefaultAddress(Long userId) {
        update(new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0));
    }
}
