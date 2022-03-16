package ru.lauk.chartographer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@ComponentScan("ru.lauk.chartographer")
public class ApplicationConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer ppc =
                new PropertySourcesPlaceholderConfigurer();

        ppc.setLocation(new ClassPathResource("application.properties"));
        ppc.setFileEncoding("UTF-8");
        ppc.setIgnoreUnresolvablePlaceholders(true);
        return ppc;
    }
}
