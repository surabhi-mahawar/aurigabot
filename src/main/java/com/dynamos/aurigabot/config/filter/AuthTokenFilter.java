package com.dynamos.aurigabot.config.filter;

import com.dynamos.aurigabot.config.security.UserDetailsSecurityService;
import com.dynamos.aurigabot.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsSecurityService userDetailsSecurityService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestToken = request.getHeader("Authorization");
        String username = null;
        String token = null;
        System.out.println("Filter called !");
        if (request != null && requestToken != null &&requestToken.startsWith("Bearer")){
            token = requestToken.substring(7);

            try{
                username = jwtUtils.extractUsername(token);
            } catch(IllegalArgumentException e){

            } catch(ExpiredJwtException e){

            } catch(MalformedJwtException e){

            }
        } else {
            //jwt is not starts with Bearer or request is null
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = userDetailsSecurityService.loadUserByUsername(username);

            if (jwtUtils.validateToken(token,userDetails)){
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            } else {
                //Invalid jwt token
            }
        } else {
            // username is null or context is not null
        }
        filterChain.doFilter(request,response);
    }

    }