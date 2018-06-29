package com.aabanegas.payment.configuration;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    /**
     * Provides generic defaults and convenience methods for Springfox/Swagger framework configuration.
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .securitySchemes(newArrayList(new ApiKey("authorization", "authorization", "header")))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.aabanegas.payment"))
                .paths(PathSelectors.any()).build()
                .pathMapping("/")
                .protocols(newHashSet("http", "https"))
                .directModelSubstitute(LocalDate.class, String.class)
                .genericModelSubstitutes(ResponseEntity.class)
                .useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.GET, responseMessages());
    }

    /**
     * @return Swagger object with API info
     */
    private ApiInfo apiInfo() {
        return new ApiInfo("Payments API", "API to manage Payments.",
                "v1", "Terms of service", new Contact("Aurelio Aragones", "", "aurelio.aragones@gmail.com"), "", "");
    }

    /**
     * @return Set of generic response messages that override the default/global response messages
     */
    private List<ResponseMessage> responseMessages() {
        return newArrayList(new ResponseMessageBuilder()
                        .code(400)
                        .message("An input validation error occurred. One or more input fields are invalid.").build(),
                new ResponseMessageBuilder()
                        .code(401)
                        .message("Unauthorized - JWT validation failed").build(),
                new ResponseMessageBuilder()
                        .code(404)
                        .message("Not found").build(),
                new ResponseMessageBuilder()
                        .code(503)
                        .message("The service is unavailable").build());
    }
}
