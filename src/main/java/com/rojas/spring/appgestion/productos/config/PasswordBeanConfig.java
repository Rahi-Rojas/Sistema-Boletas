package com.rojas.spring.appgestion.productos.config; // <--- Cambiado a tu proyecto actual

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordBeanConfig {

    @Bean
    public PasswordEncoder passwordEncoder() { // <-- Cambia el retorno de BCryptPasswordEncoder a PasswordEncoder
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // Este main es súper útil para generar tus contraseñas manualmente
    public static void main(String[] args){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // Cuando quieras una nueva contraseña para la BD, cámbiala aquí y corre esta clase
        System.out.println("Hash para 'admin123': " + encoder.encode("admin123"));
    }
}