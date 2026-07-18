package com.rojas.spring.appgestion.productos;

import com.rojas.spring.appgestion.productos.Controller.AuthController;
import com.rojas.spring.appgestion.productos.Model.Request.LoginRequest;
import com.rojas.spring.appgestion.productos.Model.Response.UserResponse;
import com.rojas.spring.appgestion.productos.Service.JwtService;
import com.rojas.spring.appgestion.productos.Service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    @Test
    void cuandoLoginExitoso_entoncesRetornarTokenYUsuario() {
        // ARRANGE
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .username("admin")
                .role("ADMIN")
                .build();

        when(userService.login(any(LoginRequest.class))).thenReturn(userResponse);
        when(jwtService.generateToken("admin")).thenReturn("jwt-token-123");

        // ACT
        ResponseEntity<?> response = authController.login(request);

        // ASSERT
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("jwt-token-123", body.get("token"));
        assertEquals("admin", body.get("username"));
        assertEquals("ADMIN", body.get("role"));
        assertEquals(1L, body.get("id"));

        verify(userService, times(1)).login(any(LoginRequest.class));
        verify(jwtService, times(1)).generateToken("admin");
    }

    @Test
    void cuandoCredencialesInvalidas_entoncesLanzarExcepcion() {
        // ARRANGE
        LoginRequest request = new LoginRequest();
        request.setUsername("malo");
        request.setPassword("xxx");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Credenciales inválidas"));

        // ACT & ASSERT
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            authController.login(request);
        });

        assertEquals("Credenciales inválidas", ex.getMessage());
        verify(userService, times(1)).login(any(LoginRequest.class));
        verify(jwtService, never()).generateToken(anyString());
    }
}
