package com.company;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static com.company.constant.FileConstant.USER_FOLDER;
import static com.company.constant.SecurityConstant.*;

@SpringBootApplication
public class BugTrackerApiApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(SpringBootLoginRegisterApiApplication.class, args);
		new File(USER_FOLDER).mkdirs();
	}

	@Bean
	public org.springframework.web.filter.CorsFilter corsFilter()
	{
		UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.setAllowCredentials(true);
		corsConfiguration.setAllowedOrigins(Collections.singletonList(ALLOWED_ORIGIN));
		corsConfiguration.setAllowedHeaders(Arrays.asList(ALLOWED_HEADERS));
		corsConfiguration.setExposedHeaders(Arrays.asList(EXPOSED_HEADERS));
		corsConfiguration.setAllowedMethods(Arrays.asList(ALLOWED_METHODS));
		urlBasedCorsConfigurationSource.registerCorsConfiguration(PATTERN, corsConfiguration);
		return new CorsFilter(urlBasedCorsConfigurationSource);
	}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder()
	{
		return new BCryptPasswordEncoder();
	}

}
