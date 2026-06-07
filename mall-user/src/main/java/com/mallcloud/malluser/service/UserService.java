package com.mallcloud.malluser.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mallcloud.malluser.api.dto.UserRegisterDTO;
import com.mallcloud.malluser.api.dto.UserUpdateDTO;
import com.mallcloud.malluser.api.vo.UserVO;
import com.mallcloud.malluser.domain.entity.User;

public interface UserService extends IService<User> {
    void register(UserRegisterDTO dto);
    UserVO getCurrentUser();
    void updateCurrentUser(UserUpdateDTO dto);
}
