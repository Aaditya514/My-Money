package com.financetracker;

import com.financetracker.dto.AuthResponse;
import com.financetracker.dto.LoginRequest;
import com.financetracker.dto.RegisterRequest;
import com.financetracker.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testRegisterAndLogin() {
        RegisterRequest registerReq = new RegisterRequest("Test User", "test@example.com", "password123", "9876543205");
        AuthResponse regResponse = userService.register(registerReq);

        assertTrue(regResponse.isSuccess());
        assertNotNull(regResponse.getUser());
        assertEquals("Test User", regResponse.getUser().getName());
        assertEquals("test@example.com", regResponse.getUser().getEmail());
        assertEquals("9876543205", regResponse.getUser().getPhoneNumber());

        LoginRequest loginReq = new LoginRequest("test@example.com", "password123");
        AuthResponse loginResponse = userService.login(loginReq);

        assertTrue(loginResponse.isSuccess());
        assertEquals("test@example.com", loginResponse.getUser().getEmail());
    }

    @Test
    void testLoginWithPhoneNumber() {
        RegisterRequest registerReq = new RegisterRequest("Phone User", "phoneuser@example.com", "password123", "9876543207");
        userService.register(registerReq);

        LoginRequest loginReq = new LoginRequest("9876543207", "password123");
        AuthResponse loginResponse = userService.login(loginReq);

        assertTrue(loginResponse.isSuccess());
        assertEquals("phoneuser@example.com", loginResponse.getUser().getEmail());
    }

    @Test
    void testRegisterMissingPhoneNumber_ThrowsException() {
        RegisterRequest registerReq = new RegisterRequest("No Phone User", "nophone@example.com", "password123");
        // phoneNumber is null, verifying strictly mandatory requirement
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.register(registerReq));
        assertTrue(ex.getMessage().contains("Mobile number is required for Splitwise group matching"));
    }

    @Test
    void testLoginWithInvalidPassword() {
        RegisterRequest registerReq = new RegisterRequest("John Doe", "john@example.com", "secret123", "9876543206");
        userService.register(registerReq);

        LoginRequest loginReq = new LoginRequest("john@example.com", "wrongpassword");
        assertThrows(RuntimeException.class, () -> userService.login(loginReq));
    }
}
