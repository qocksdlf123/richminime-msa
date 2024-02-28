package com.example.user.config.util;

import com.example.user.config.common.security.CustomUserDetails;
import com.example.user.config.exception.ForbiddenException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SecurityUtils {

    private Authentication authentication;
    private CustomUserDetails userPrincipal;

    private void getAuthentication() {
        try {
            Authentication authentication = Objects.requireNonNull(SecurityContextHolder
                    .getContext()
                    .getAuthentication());

            if (authentication instanceof AnonymousAuthenticationToken) {
                authentication = null;
            }

            this.authentication = authentication;
            this.userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        } catch (NullPointerException e) {
            throw new ForbiddenException();
        }
    }

    public String getLoggedInUserEmail() {
//        try {
////            Authentication authentication = Objects.requireNonNull(SecurityContextHolder
////                    .getContext()
////                    .getAuthentication());
////
////            if (authentication instanceof AnonymousAuthenticationToken) {
////                authentication = null;
////            }
//
//            getAuthentication();
//
//            //이메일정보
//            return authentication.getName();
//        } catch (NullPointerException e) {
//            throw new ForbiddenException();
//        }
//
        getAuthentication();
        return authentication.getName();
    }

    public Long getUserNo() {
//        try {
////            Authentication authentication = Objects.requireNonNull(SecurityContextHolder
////                    .getContext()
////                    .getAuthentication());
////
////            if (authentication instanceof AnonymousAuthenticationToken) {
////                authentication = null;
////            }
////
////            CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
//
//            getAuthentication();
//
//            // userId(번호)
//            return userPrincipal.getUserId();
//        } catch (NullPointerException e) {
//            throw new ForbiddenException();
//        }
        getAuthentication();
        return userPrincipal.getUserId();
    }

}

