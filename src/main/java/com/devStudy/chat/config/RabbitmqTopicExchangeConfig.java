package com.devStudy.chat.config;

import static com.devStudy.chat.service.utils.ConstantValues.RABBITMQ_QUEUE_Q1;
import static com.devStudy.chat.service.utils.ConstantValues.RABBITMQ_QUEUE_Q2;
import static com.devStudy.chat.service.utils.ConstantValues.RABBITMQ_EXCHANGE_NAME;
import static com.devStudy.chat.service.utils.ConstantValues.ROUTING_KEY_RET_PASSWORD;
import static com.devStudy.chat.service.utils.ConstantValues.ROUTING_KEY_VERIF_CODE;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqTopicExchangeConfig {
    @Bean Queue queue() { return new Queue(RABBITMQ_QUEUE_Q1); }

    @Bean Queue queue2() { return new Queue(RABBITMQ_QUEUE_Q2); }

    @Bean TopicExchange exchange() { return new TopicExchange(RABBITMQ_EXCHANGE_NAME); }

    @Bean
    Binding queueBinding(){
        return BindingBuilder.bind(queue()).to(exchange()).with(ROUTING_KEY_RET_PASSWORD);
    }

    @Bean
    Binding queueBinding2(){
        return BindingBuilder.bind(queue2()).to(exchange()).with(ROUTING_KEY_VERIF_CODE);
    }
}
