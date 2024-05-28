package com.freshshop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

import com.freshshop.service.CustomOAuth2UserService;


@Configuration
public class FreshShopSecurityConfig {


	
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .requestMatchers("/home").permitAll()
                .requestMatchers("/cart").authenticated()
                .requestMatchers("/profile").authenticated()
                .requestMatchers("/addToCart").authenticated()
                .requestMatchers("/viewCart").authenticated()
                .requestMatchers("/admin/**").authenticated()
                .requestMatchers("/admin/**").permitAll()
                .requestMatchers("/shop/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/shop/page/**").permitAll()
                .requestMatchers("/getProductsByCate").permitAll()
                .requestMatchers("/about").permitAll()
                .requestMatchers("/shop-detail").permitAll()
                .requestMatchers("/displayDetail").permitAll()
                .requestMatchers("/checkout").authenticated()
                .requestMatchers("/placeOrder").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin").hasRole("ADMIN")
                .requestMatchers("/report/**").hasRole("ADMIN")
                .requestMatchers("/report").hasRole("ADMIN")
                .requestMatchers("/login").permitAll()
                .requestMatchers("/logout").permitAll()
                .requestMatchers("/register").permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/vnpay-payment/**").permitAll()
                .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
                .and()
                .formLogin()
                    .loginPage("/login")
                    .defaultSuccessUrl("/home")
                    .failureUrl("/login?error=true")
                    .permitAll()
                    .and()
                    .oauth2Login()
                    .loginPage("/login")
                    .defaultSuccessUrl("/loginSuccess", true)
                    
                .and()
                .oauth2Login()
                .loginPage("/login")
                .defaultSuccessUrl("/home")
                .failureUrl("/login?error=true")
                .permitAll()
                .and()
                .logout()
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .permitAll()
                .and()
                .httpBasic();
        

        return http.build();
    }
    

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
   

    
}