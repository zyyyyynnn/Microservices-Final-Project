package com.mallcloud.malluser.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mallcloud.malluser.api.dto.AddressDTO;
import com.mallcloud.malluser.api.vo.AddressVO;
import com.mallcloud.malluser.domain.entity.Address;

import java.util.List;

public interface AddressService extends IService<Address> {
    List<AddressVO> listCurrentUserAddresses();
    void addAddress(AddressDTO dto);
    void updateAddress(Long id, AddressDTO dto);
    void deleteAddress(Long id);
}
