package com.freshshop.model;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomOAuth2User extends DefaultOAuth2User {

    public CustomOAuth2User(OAuth2User oAuth2User) {
        super(oAuth2User.getAuthorities(), oAuth2User.getAttributes(), "name");
    }

    public String getGoogleAccountId() {
        return getAttribute("sub");
    }
}