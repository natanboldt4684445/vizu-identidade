package com.vizu.identidade.integracao.config;
import org.springframework.amqp.core.TopicExchange; import org.springframework.amqp.support.converter.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.*;
@Configuration public class RabbitConfig { @Bean TopicExchange vizuEventsExchange(@Value("${vizu.messaging.exchange}") String name){return new TopicExchange(name,true,false);}@Bean MessageConverter messageConverter(){return new Jackson2JsonMessageConverter();} }
