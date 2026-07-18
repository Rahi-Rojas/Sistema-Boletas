package com.rojas.spring.appgestion.productos;

import com.rojas.spring.appgestion.productos.Exception.ApiErrorException;
import com.rojas.spring.appgestion.productos.Mapper.UserMapper;
import com.rojas.spring.appgestion.productos.Model.Request.LoginRequest;
import com.rojas.spring.appgestion.productos.Model.Response.UserResponse;
import com.rojas.spring.appgestion.productos.Model.User;
import com.rojas.spring.appgestion.productos.Repository.UserRepository;
import com.rojas.spring.appgestion.productos.Service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private LoginRequest buildLoginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private User buildActiveUser(String username, String encodedPassword) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setRole("ADMIN");
        user.setIsActive(true);
        return user;
    }

    private User buildInactiveUser(String username) {
        User user = new User();
        user.setId(2L);
        user.setUsername(username);
        user.setPassword("hash");
        user.setRole("USER");
        user.setIsActive(false);
        return user;
    }

    @Test
    void cuandoLoginCredencialesValidas_entoncesRetornarUserResponse() {
        LoginRequest request = buildLoginRequest("admin", "123456");
        User user = buildActiveUser("admin", "$2a$encoded");

        UserResponse response = UserResponse.builder()
                .id(1L)
                .username("admin")
                .role("ADMIN")
                .build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "$2a$encoded")).thenReturn(true);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse resultado = userService.login(request);

        assertNotNull(resultado);
        assertEquals("admin", resultado.username());
        assertEquals("ADMIN", resultado.role());
        verify(userRepository, times(1)).findByUsername("admin");
        verify(passwordEncoder, times(1)).matches("123456", "$2a$encoded");
    }

    @Test
    void cuandoLoginUsuarioNoExiste_entoncesLanzarCredencialesInvalidas() {
        LoginRequest request = buildLoginRequest("fantasma", "123");

        when(userRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        ApiErrorException ex = assertThrows(ApiErrorException.class, () -> {
            userService.login(request);
        });

        assertEquals("Credenciales inválidas", ex.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void cuandoLoginCuentaDesactivada_entoncesLanzarCredencialesInvalidas() {
        LoginRequest request = buildLoginRequest("bloqueado", "123456");
        User inactiveUser = buildInactiveUser("bloqueado");

        when(userRepository.findByUsername("bloqueado")).thenReturn(Optional.of(inactiveUser));

        ApiErrorException ex = assertThrows(ApiErrorException.class, () -> {
            userService.login(request);
        });

        assertEquals("Credenciales inválidas", ex.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void cuandoLoginContrasenaIncorrecta_entoncesLanzarCredencialesInvalidas() {
        LoginRequest request = buildLoginRequest("admin", "mala");
        User user = buildActiveUser("admin", "$2a$encoded");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("mala", "$2a$encoded")).thenReturn(false);

        ApiErrorException ex = assertThrows(ApiErrorException.class, () -> {
            userService.login(request);
        });

        assertEquals("Credenciales inválidas", ex.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        verify(userMapper, never()).toResponse(any());
    }
}
