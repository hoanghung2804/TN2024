package com.freshshop.controller.UserController;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.cloudinary.Api.HttpMethod;
import com.cloudinary.utils.StringUtils;
import com.freshshop.constant.FreshShopConstants;
import com.freshshop.model.Customer;
import com.freshshop.model.Roles;
import com.freshshop.repository.CustomerRepository;
import com.freshshop.repository.RoleRepository;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class LoginController {

	  @Autowired
	    private CustomerRepository customerRepository;

	    @Autowired
	    private RoleRepository roleRepository;

	    @Autowired
	    private PasswordEncoder passwordEncoder;

	private final MessageSource messageSource;

	@Autowired
	public LoginController(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Autowired
	HttpSession session;

	@RequestMapping(value = "/login", method = { RequestMethod.GET, RequestMethod.POST })
	public String displayLoginPage(Model model, @RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout,
			@RequestParam(value = "register", required = false) String register) {

		String message = "";
		if (error != null) {
			message = messageSource.getMessage("login.failed", null, LocaleContextHolder.getLocale());
		}
		if (logout != null) {
			message = messageSource.getMessage("logout.success", null, LocaleContextHolder.getLocale());
		}
		if (register != null) {
			message = messageSource.getMessage("register.success", null, LocaleContextHolder.getLocale());
		}
		model.addAttribute("message", message);
		return "User/login.html";
	}
	
	@RequestMapping(value = "/oauth2/authorization/google")
    public String redirectToGoogle() {
        return "redirect:/login";
    }

	@RequestMapping(value = "/login/oauth2/code/google")
	public String loginWithGoogle() {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (authentication.isAuthenticated()) {
	        OAuth2LoginAuthenticationToken token = (OAuth2LoginAuthenticationToken) authentication;
	        
	        // Getting additional information from Google account.
	        String email = token.getPrincipal().getAttribute("email");
	 
//	        String picture = token.getPrincipal().getAttribute("picture");
//	        String locale = token.getPrincipal().getAttribute("locale");

	        // Searching for a Customer in database with the received email.
	        Customer customer = customerRepository.readByEmail(email);
	        
	        
	        // Checking if we have a new Customer or an old one.
	        if (customer == null) {
	            // New Customer logic.
	            // Set picture, locale or any additional information if necessary.
	        	 Roles roles = roleRepository.getByRoleName(FreshShopConstants.USER_ROLE);
	        	customer.setRoles(roles);
		        customer.setStatus(FreshShopConstants.OPEN);
	            customer = new Customer();
	            //customer.setEmail(email);
	            customer.setCustomerName(token.getPrincipal().getAttribute("name"));
	            //customer.setPwd(new BCryptPasswordEncoder().encode(token.getPrincipal().getAttribute("sub")));
	            
	            customerRepository.save(customer);
	        } else {
	            // Existing Customer logic.
	            // Update picture, locale or any additional information if necessary.
	        }
	        
	        session.setAttribute("customer", customer);
	        
	        return "redirect:/home";
	    } else {
	        return "redirect:/login?error=true";
	    }
	}
    //... your existing codes ...
}
	 
	
//    @GetMapping("/loginSuccess")
//    public String getLoginInfo(Model model, OAuth2AuthenticationToken authentication) {
//        OAuth2User user = authentication.getPrincipal();
//        // Save the user's information to your database here
//        return "User/login.html";  // Return the name of the page you want to redirect the user to
//    }

