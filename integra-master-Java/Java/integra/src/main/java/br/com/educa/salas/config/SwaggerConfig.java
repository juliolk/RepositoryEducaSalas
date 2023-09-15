package br.com.educa.salas.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;


@Configuration

public class SwaggerConfig {
	
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.servers(Collections
	            .singletonList(new Server().url("http://localhost:9030")))
				.components(new Components())
				.info(new Info().title("EducaSalas").version("V1").description(
                        "Educa")
						.license(new License().name("Documentação").url("http://localhost:9030/swagger-ui.html")));
	}
	

	//@Value("${br.com.educa.salas.controller}")
	//private String swaggerPath;
	
	/*@Bean
	  public OpenAPI springShopOpenAPI() {
	      return new OpenAPI()
	              .info(new Info().title("SpringShop API")
	              .description("Spring shop sample application")
	              .version("v0.0.1")
	              .license(new License().name("Apache 2.0").url("http://springdoc.org")))
	              .externalDocs(new ExternalDocumentation()
	              .description("SpringShop Wiki Documentation")
	              .url("https://springshop.wiki.github.org/docs"));
	  }*/

}