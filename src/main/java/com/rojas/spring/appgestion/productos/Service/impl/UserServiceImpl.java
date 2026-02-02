package com.rojas.spring.appgestion.productos.Service.impl;

import com.rojas.spring.appgestion.productos.Exception.ApiErrorException;
import com.rojas.spring.appgestion.productos.Mapper.UserMapper;
import com.rojas.spring.appgestion.productos.Model.Request.UserRequest;
import com.rojas.spring.appgestion.productos.Model.Response.UserResponse;
import com.rojas.spring.appgestion.productos.Model.User;
import com.rojas.spring.appgestion.productos.Repository.UserRepository;
import com.rojas.spring.appgestion.productos.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public UserResponse create(UserRequest request) {
        User entity = userMapper.toEntity(request);
        entity.setPassword(passwordEncoder.encode(request.getPassword()));
        entity.setIsActive(true); // Aseguramos que inicie activo
        return userMapper.toResponse(userRepository.save(entity));
    }

    @Override
    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ApiErrorException("Usuario no encontrado", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<UserResponse> findAll() {
        // CORRECCIÓN: Se elimina el filtro para mostrar activos y desactivados (Auditoría)
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse update(Long id, UserRequest request) {
        return userRepository.findById(id)
                .map(existing -> {
                    userMapper.updateFromRequest(request, existing);
                    if (request.getPassword() != null && !request.getPassword().isBlank()) {
                        existing.setPassword(passwordEncoder.encode(request.getPassword()));
                    }
                    return userMapper.toResponse(userRepository.save(existing));
                })
                .orElseThrow(() -> new ApiErrorException("No se pudo actualizar", HttpStatus.NOT_FOUND));
    }

    //todo el poderoso delete soft
    @Override
    public void deleteById(Long id) {
        userRepository.findById(id).ifPresentOrElse(user -> {
            // Protección: Un administrador no puede ser desactivado
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                throw new ApiErrorException("No se puede desactivar a un administrador", HttpStatus.FORBIDDEN);
            }
            user.setIsActive(false);
            userRepository.save(user);
        }, () -> {
            throw new ApiErrorException("ID no encontrado", HttpStatus.NOT_FOUND);
        });
    }

    public void hardDeleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ApiErrorException("Usuario no encontrado para eliminación física", HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserResponse login(com.rojas.spring.appgestion.productos.Model.Request.LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ApiErrorException("Usuario no encontrado", HttpStatus.UNAUTHORIZED));
        // CORRECCIÓN: Validar si el usuario está activo antes de dejarlo loguear
        if (!user.getIsActive()) {
            throw new ApiErrorException("La cuenta está desactivada. Contacte al administrador.", HttpStatus.FORBIDDEN);
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new ApiErrorException("Contraseña incorrecta", HttpStatus.UNAUTHORIZED);
        }
        return userMapper.toResponse(user);
    }
}