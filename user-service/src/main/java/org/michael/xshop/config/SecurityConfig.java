package org.michael.xshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register",
                                "/auth/login",
                                "/users/me"
                                ).permitAll() // 放行这两个接口
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable()); // 如果是前后端分离项目，通常需要禁用
        return http.build();
    }
}