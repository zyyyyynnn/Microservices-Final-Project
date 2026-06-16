package com.mallcloud.mallcommon.config;

import com.mallcloud.mallcommon.exception.BizException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class InternalAuthPropertiesValidatorTest {

    private InternalAuthPropertiesValidator validator(String[] activeProfiles) {
        MockEnvironment env = new MockEnvironment();
        if (activeProfiles != null) {
            env.setActiveProfiles(activeProfiles);
        }
        return new InternalAuthPropertiesValidator(new InternalAuthProperties(), env);
    }

    @Test
    void devProfileWithDefaultTokenIsAllowed() {
        assertDoesNotThrow(() -> validator(new String[]{"dev"}).validate());
    }

    @Test
    void localProfileWithBlankTokenIsAllowed() {
        InternalAuthProperties props = new InternalAuthProperties();
        props.setToken("");
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("local");
        assertDoesNotThrow(() -> new InternalAuthPropertiesValidator(props, env).validate());
    }

    @Test
    void testProfileWithNullTokenIsAllowed() {
        InternalAuthProperties props = new InternalAuthProperties();
        props.setToken(null);
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("test");
        assertDoesNotThrow(() -> new InternalAuthPropertiesValidator(props, env).validate());
    }

    @Test
    void noActiveProfileIsTreatedAsLocalDefault() {
        assertDoesNotThrow(() -> validator(null).validate());
    }

    @Test
    void prodProfileWithDefaultTokenFailsFast() {
        BizException ex = assertThrows(BizException.class,
                () -> validator(new String[]{"prod"}).validate());
        assertEquals(401, ex.getCode());
    }

    @Test
    void stagingProfileWithBlankTokenFailsFast() {
        InternalAuthProperties props = new InternalAuthProperties();
        props.setToken("   ");
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("staging");
        assertThrows(BizException.class,
                () -> new InternalAuthPropertiesValidator(props, env).validate());
    }

    @Test
    void fullProfileWithNullTokenFailsFast() {
        InternalAuthProperties props = new InternalAuthProperties();
        props.setToken(null);
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("full");
        assertThrows(BizException.class,
                () -> new InternalAuthPropertiesValidator(props, env).validate());
    }

    @Test
    void prodProfileWithExplicitTokenIsAllowed() {
        InternalAuthProperties props = new InternalAuthProperties();
        props.setToken("prod-strong-secret-2026");
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        assertDoesNotThrow(() -> new InternalAuthPropertiesValidator(props, env).validate());
    }
}
