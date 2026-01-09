package com.tesis.gamificacion.config

import org.apache.logging.log4j.util.Supplier
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
class RestConfig {
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate{
        return builder
            .requestFactory(SimpleClientHttpRequestFactory::class.java)
            .connectTimeout(Duration.ofSeconds(1000000))
            .readTimeout(Duration.ofSeconds(6000000)).build()
    }
}