/*
 * Copyright (C) Grégory Van den Borre - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Grégory Van den Borre <vandenborre.gregory@hotmail.com> 2019
 */

package be.yildizgames.authentication.infrastructure.web.spring;

import be.yildizgames.authentication.main.AuthenticationEntryPoint;
import be.yildizgames.module.webapp.WebApplication;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class SpringWebApp extends SpringBootServletInitializer implements WebApplication {

    private final SpringApplication springApplication;

    public SpringWebApp(String[] args) {
        AuthenticationEntryPoint entryPoint = AuthenticationEntryPoint.create();
        entryPoint.start(args);
        this.springApplication = new SpringApplication(SpringWebApp.class);
        this.springApplication.setBannerMode(Banner.Mode.OFF);
    }

    @Override
    public WebApplication start() {
        this.springApplication.run();
        return this;
    }
}
