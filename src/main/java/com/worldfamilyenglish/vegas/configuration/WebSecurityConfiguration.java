package com.worldfamilyenglish.vegas.configuration;

import com.azure.spring.cloud.autoconfigure.aad.AadJwtBearerTokenAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

@Component
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration {

    @Bean
    @Profile("!test")
    public SecurityFilterChain filterChain(final HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors()
                .and()
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/h2-console/**").permitAll()
                .and().authorizeRequests().antMatchers( "/h2-console").authenticated()
                .antMatchers("/graphiql").permitAll()
                .and().authorizeRequests().antMatchers( "/graphql").authenticated()
                .and()
                .authorizeRequests((requests) -> requests.anyRequest().authenticated())
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(new AadJwtBearerTokenAuthenticationConverter());

        httpSecurity.headers().frameOptions().disable();
        
        return httpSecurity.build();
    }

    @Bean
    @Profile("test")
    public SecurityFilterChain testFilterChain(final HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors()
                .and()
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/graphql").permitAll();


        httpSecurity.headers().frameOptions().disable();

        return httpSecurity.build();
    }
}