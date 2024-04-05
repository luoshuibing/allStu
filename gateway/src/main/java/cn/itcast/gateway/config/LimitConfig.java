package cn.itcast.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class LimitConfig {
    /**
     * 按照 Path 访问次数限流
     *
     * @return key
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getPath().toString());
    }

//   /**
//     * 根据IP限流
//     */
//    @Bean
//    public KeyResolver ipKeyResolver() {
//        return exchange -> Mono.just(
//                exchange.getRequest()
//                        .getRemoteAddress()
//                        .getHostName()
//        );
//    }

    /**
     * 根据token限流
     */
//    @Bean
//    public KeyResolver tokenKeyResolver() {
//        return new KeyResolver() {
//            @Override
//            public Mono<String> resolve(ServerWebExchange exchange) {
//                return Mono.just(exchange.getRequest().getQueryParams().getFirst("token"));
//            }
//        };
//    }

}

