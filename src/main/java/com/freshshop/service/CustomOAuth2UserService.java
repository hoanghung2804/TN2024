package com.freshshop.service;

import java.io.Console;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.freshshop.model.CustomOAuth2User;
import com.freshshop.model.Customer;
import com.freshshop.model.Roles;
import com.freshshop.repository.CustomerRepository;

import com.freshshop.repository.RoleRepository;

import jakarta.servlet.http.HttpSession;




@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    HttpSession session;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        Customer customer = customerRepository.readByEmail(email);
        if (customer == null) {
            customer = new Customer();
            customer.setEmail(email);
            customer.setCustomerName(oAuth2User.getAttribute("name"));
            // Feel free to set any default properties for the customer here.
            // Example: customer.setStatus("ACTIVE");

            // Generate a random password or set it to a predefined string as it won't be used.
//            customer.setPassword(passwordEncoder.encode("NotUsed"));

            Roles defaultRole = roleRepository.findByRoleName("ROLE_USER");
            customer.setRoles(defaultRole);

            customerRepository.save(customer);
        }

        // Here you can map the OAuth2User details to your custom OAuth2User class if needed.
        return oAuth2User;
    }
    
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//    	 OAuth2User oAuth2User = super.loadUser(userRequest);
//
//         String googleAccountId = oAuth2User.getAttribute("sub");
//
//         // Lưu trữ googleAccountId trong phiên
//         session.setAttribute("googleAccountId", googleAccountId);
//
//        CustomOAuth2User customOAuth2User = new CustomOAuth2User(oAuth2User);
//        return customOAuth2User;
//    }
}

    
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//       
//        OAuth2User oAuth2User = super.loadUser(userRequest);
//        System.out.println("OAuth2User: " + oAuth2User.toString()); // Add this line
//
//        String nameAttributeKey = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
//        CustomOAuth2User customOAuth2User = new CustomOAuth2User(oAuth2User.getAttributes(), nameAttributeKey);
//
//        String email = customOAuth2User.getEmail();
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String currentPrincipalName = authentication.getName();
//        
//        Customer customer = customerRepository.readByEmail(email);
//        if (customer == null) {
//            customer = new Customer();
//            customer.setEmail(email);
//            customer.setCustomerName(customOAuth2User.getName());
//            customer.setPwd(passwordEncoder.encode("DEFAULT_PASSWORD")); // default password or you can generate a random one
//            // Set additional fields as needed
//            customerRepository.save(customer);
//        } else {
//           // if user already exists just update his data
//             customer.setEmail(email);
//             customer.setCustomerName(customOAuth2User.getName());
//             customerRepository.save(customer);
//        }
//
//        return customOAuth2User;
 //   }
