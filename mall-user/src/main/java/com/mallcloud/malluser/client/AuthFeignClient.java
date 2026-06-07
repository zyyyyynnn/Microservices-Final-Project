package com.mallcloud.malluser.client;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.malluser.client.dto.AuthCredentialDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mall-auth", path = "/api/v1/auth")
public interface AuthFeignClient {

    @PostMapping("/internal/credentials")
    Result<Void> createCredentials(@RequestBody AuthCredentialDTO dto);
}
