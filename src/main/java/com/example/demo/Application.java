package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(CustomProperty.class)
public class Application {

    public static void main(String[] args) {
        String[] actualArgs = new String[]{"spring.datasource.url=jdbc:postgres://localhost/some-prod-db"};
        SpringApplication.run(Application.class, actualArgs);

        new SpringApplicationBuilder()
                .sources(Application.class)
                .initializers(context -> {
                    context.getEnvironment()
                            .getPropertySources()
                            .addLast(new CustomPropertySource());
                })
                .run(args);
    }

    @Bean
    ApplicationRunner applicationRunner(Environment environment) {
        return args -> {
            log.info("Our database URL connection will be " + environment.getProperty("spring.datasource.url"));
        };
    }

    @Bean
    ApplicationRunner applicationRunner(@Value("${custom-message}") String property) {
        return args -> {
            log.info("Property value is: " + property);
        };
    }
}


class CustomPropertySource extends PropertySource<String> {

    CustomPropertySource() {
        super("custom");
    }

    @Override
    public Object getProperty(String name) {

        if (name.equalsIgnoreCase("custom-message")) {
            return "Hello from " + CustomPropertySource.class.getSimpleName() + "!";
        }

        return null;
    }
}

@Data
@AllArgsConstructor
@ConstructorBinding
@ConfigurationProperties("custom")
class CustomProperty {
    private String name;
    private String value;
    private String address;
}


@ShellComponent
@RequiredArgsConstructor
class ShellManager {
    private final CustomProperty property;
    private final Environment env;

    @ShellMethod("List all properties")
    public void checkProp() {
        System.out.println("property = " + property);
    }

    @ShellMethod("Get a property base on key")
    public void getProp(final String key) {
        String value = env.getRequiredProperty(key);

        System.out.println("value = " + value);
    }

    @ShellMethod("Set a new value on address")
    public void setAddress(final String value) {
        property.setAddress(value);
    }
}

@ShellComponent
@RequiredArgsConstructor
class ShellReflect {
    private final CustomProperty property;

    @ShellMethod(value = "List all properties", prefix = "ptl")
    public void printProp() {
        System.out.println("property = " + property);
    }

}