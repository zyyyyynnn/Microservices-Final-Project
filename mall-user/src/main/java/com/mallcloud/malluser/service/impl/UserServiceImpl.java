package com.mallcloud.malluser.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallcommon.util.UserContext;
import com.mallcloud.malluser.api.dto.UserRegisterDTO;
import com.mallcloud.malluser.api.dto.UserUpdateDTO;
import com.mallcloud.malluser.api.vo.UserVO;
import com.mallcloud.malluser.client.AuthFeignClient;
import com.mallcloud.malluser.client.dto.AuthCredentialDTO;
import com.mallcloud.malluser.domain.entity.User;
import com.mallcloud.malluser.mapper.UserMapper;
import com.mallcloud.malluser.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final AuthFeignClient authFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO dto) {
        // Validate SMS code here if needed
        // For now we just verify if the user exists
        long count = count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())
                .or()
                .eq(User::getPhone, dto.getPhone()));
        if (count > 0) {
            throw new BizException(10001, "用户名或手机号已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getUsername());
        user.setStatus(1);
        user.setGmtCreate(LocalDateTime.now());
        user.setGmtModified(LocalDateTime.now());
        save(user);

        AuthCredentialDTO authDto = new AuthCredentialDTO();
        authDto.setUserId(user.getId());
        authDto.setUsername(user.getUsername());
        authDto.setPhone(user.getPhone());
        authDto.setPassword(dto.getPassword());
        
        Result<Void> authResult = authFeignClient.createCredentials(authDto);
        if (authResult == null || !authResult.isSuccess()) {
            throw new BizException(10002, "注册失败，调用认证服务异常");
        }
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = UserContext.requireUserId();
        User user = getById(userId);
        if (user == null) {
            throw new BizException(20100, "用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentUser(UserUpdateDTO dto) {
        Long userId = UserContext.requireUserId();
        User user = new User();
        user.setId(userId);
        user.setNickname(dto.getNickname());
        user.setAvatar(dto.getAvatar());
        user.setEmail(dto.getEmail());
        user.setGmtModified(LocalDateTime.now());
        updateById(user);
    }
}
