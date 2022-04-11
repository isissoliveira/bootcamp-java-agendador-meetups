package com.bootcamp.microservicemeetup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket authApi (){
        return new Docket (DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.bootcamp.microservicemeetup"))
                .paths(regex("/api/registration.*"))
                .build()
                .groupName("Registration")
                .apiInfo(metaInfo());
    }

    private ApiInfo metaInfo() {
        ApiInfo apiInfo = new ApiInfo(
                "Registration API Rest",
                "API Rest para cadastro de registrations",
                "1.0",
                "Termos de Serviço",
                new Contact("Isis Oliveira", "","isissoliveira@gmail.com"),
                "Apache License Version 2.0",
                "", new ArrayList<VendorExtension>()
        );
        return apiInfo;
    }
}
