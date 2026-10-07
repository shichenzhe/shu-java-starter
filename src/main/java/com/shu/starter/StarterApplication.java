package com.shu.starter;

import com.shu.starter.common.config.AppProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProps.class)
public class StarterApplication {
  public static void main(String[] args) {
    SpringApplication.run(StarterApplication.class, args);
  }
}
