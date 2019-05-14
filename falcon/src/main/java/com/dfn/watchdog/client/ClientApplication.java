package com.dfn.watchdog.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.MimeMappings;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fire up the spring-boot application.
 */

@SpringBootApplication
@RestController
public class ClientApplication {

    public void run() {
        SpringApplication.run(ClientApplication.class);
    }

    @RequestMapping("/user")
    public Principal user(Principal user) {
        return user;
    }

    @RequestMapping("/resource")
    public Map<String, Object> home() {
        Map<String, Object> model = new HashMap<>();
        model.put("id", UUID.randomUUID().toString());
        model.put("content", "Hello World");
        return model;
    }

    @RequestMapping("/server/config")
    public Map<String, Object> getIp() {
        Map<String, Object> config = new HashMap<>();
        config.put("websocketUrl", WatchdogClient.INSTANCE.getProperties().websocketUrl());
        return config;
    }

    @Configuration
    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
    protected static class SecurityConfiguration extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .httpBasic().and()
                    .authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .antMatchers(HttpMethod.OPTIONS, "/*").permitAll()
                    .antMatchers(HttpMethod.OPTIONS, "**/").permitAll()
                    .antMatchers("/index.html", "/home.html", "/login.html",
                            "/dashboard.html", "/", "/img/*", "/server/config", "/watchdogclient/sessions", "/watchdogclient/messages", "/watchdogclient/messages/sla",
                            "/watchdogclient/responses", "/watchdogclient/messages/graph", "/watchdogclient/services", "/watchdogclient/clientCounts",
                            "/watchdogclient/clientCountMap","/watchdogclient/view","/watchdogclient/route/**","/watchdogclient/sessions/active",
                            "/watchdogclient/messages/specific**","/watchdogclient/responses/specific**","/watchdogclient/slaconfigroundtime","/watchdogclient/slaconfigdefaulttime","/watchdogclient/slaconfigservice").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .csrf().disable();
//                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
        }
    }

    @Configuration
    @EnableWebMvc
    public class WebConfig extends WebMvcConfigurerAdapter {
        @Override
        public void addCorsMappings(CorsRegistry registry) {

            registry.addMapping("/**");

        }
    }
}
