package cn.itcast.feign.config;

import cn.itcast.feign.clients.fallback.UserClientFallBackFactory;
import feign.Logger;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfiguration {
    @Bean
    public Logger.Level logLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public UserClientFallBackFactory fallBackFactory() {
        return new UserClientFallBackFactory();
    }
}
