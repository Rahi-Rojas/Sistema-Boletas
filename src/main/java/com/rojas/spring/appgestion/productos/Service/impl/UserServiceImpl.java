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

    @Override
    public UserResponse create(UserRequest request) {
        User entity = userMapper.toEntity(request);
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
        return userRepository.findAll().stream()
                .filter(User::getIsActive)
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse update(Long id, UserRequest request) {
        return userRepository.findById(id)
                .map(existing -> {
                    userMapper.updateFromRequest(request, existing);
                    return userMapper.toResponse(userRepository.save(existing));
                })
                .orElseThrow(() -> new ApiErrorException("No se pudo actualizar el usuario", HttpStatus.NOT_FOUND));
    }

    @Override
    public void deleteById(Long id) {
        userRepository.findById(id).ifPresentOrElse(user -> {
            user.setIsActive(false);
            userRepository.save(user);
        }, () -> { throw new ApiErrorException("ID no encontrado", HttpStatus.NOT_FOUND); });
    }
}