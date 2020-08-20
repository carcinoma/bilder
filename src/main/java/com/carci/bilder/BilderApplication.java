package com.carci.bilder;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching
public class BilderApplication {

	public static void main(String[] args) throws IOException {
        System.out.println(new ClassPathResource("sample.jks").getURL());
	    SpringApplication.run(BilderApplication.class, args);
	}

}

@Configuration
@Log
class StaticResourceConfiguration implements WebMvcConfigurer {

    @Value("${imagesPath}")
    String imagesPath;

    @Value("${cacheDir}")
    String cacheDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

        if(!imagesPath.endsWith("/")) {
            imagesPath += "/";
        }

        if(!cacheDir.endsWith("/")) {
            cacheDir += "/";
        }

        registry.addResourceHandler("/data/**").addResourceLocations("file://" + imagesPath);
        if (!cacheDir.startsWith("/")) {
            cacheDir = System.getProperty("user.dir") + "/" + cacheDir;
        }
        log.info("Cache Dir: " + cacheDir);
        registry.addResourceHandler("/thumbnails/**").addResourceLocations("file://"+ cacheDir +"thumbnails/").setCachePeriod(365*24*60*60);
        registry.addResourceHandler("/full-size/**").addResourceLocations("file://"+ cacheDir +"full-size/").setCachePeriod(365*24*60*60);

	}

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/notFound").setViewName("forward:/index.html");
    }

    @Bean
    public ErrorPageRegistrar errorPageRegistrar(){
        return registry -> registry.addErrorPages(
                new ErrorPage(HttpStatus.NOT_FOUND, "/notFound")
        );
    }

}

