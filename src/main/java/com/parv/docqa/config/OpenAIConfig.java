package com.parv.docqa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;

public class OpenAIConfig {

    @Bean
    public OpenAPI docuSenseOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("DocuSense API")
                        .description("AI-Powered Document Q&A and Resume Analyzer API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Parv Kothari")
                                .email("parvkothari007@gmail.com")
                                .url("https://github.com/parv-07/docusense")
                        )
                );
    }
}
