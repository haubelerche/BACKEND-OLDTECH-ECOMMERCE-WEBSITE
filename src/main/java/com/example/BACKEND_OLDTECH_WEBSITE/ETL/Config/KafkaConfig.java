package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@ConditionalOnProperty(name = "etl.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:oldtech-etl-group}")
    private String groupId;

    @Bean
    @ConditionalOnProperty(name = "etl.enabled", havingValue = "true")
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Ensure data reliability
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
          // Giảm logging ở spring lại nhìn đỡ sợ
        props.put("metric.reporters", "");
        props.put("metrics.recording.level", "INFO");
        
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    @ConditionalOnProperty(name = "etl.enabled", havingValue = "true")
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    @ConditionalOnProperty(name = "etl.enabled", havingValue = "true")
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
          // Giảm logging ở spring 
        props.put("metric.reporters", "");
        props.put("metrics.recording.level", "INFO");
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    @ConditionalOnProperty(name = "etl.enabled", havingValue = "true")
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL);
        
        // Tối ưu hóa cho số lượng consumer nhỏ
        factory.setConcurrency(1); // Mỗi topic có 1 consumer mặc định
        
        return factory;
    }

    // ===============================
    // TOPIC CREATION & ADMIN CONFIG
    // ===============================

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * AdminClient bean for health checks and topic management
     */
    @Bean
    public AdminClient adminClient() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return AdminClient.create(configs);
    }

    /**
     * ETL Sales Metrics Topic
     */
    @Bean
    public NewTopic etlSalesMetricsTopic() {
        return new NewTopic("etl-sales-metrics", 1, (short) 1)
            .configs(Map.of(
                "retention.ms", "604800000", // 7 days retention
                "compression.type", "snappy"
            ));
    }

    /**
     * ETL Business KPIs Topic
     */
    @Bean
    public NewTopic etlBusinessKpisTopic() {
        return new NewTopic("etl-business-kpis", 1, (short) 1)
            .configs(Map.of(
                "retention.ms", "604800000",
                "compression.type", "snappy"
            ));
    }

    /**
     * ETL Data Quality Alerts Topic
     */
    @Bean
    public NewTopic etlDataAlertsTopic() {
        return new NewTopic("etl-data-alerts", 1, (short) 1)
            .configs(Map.of(
                "retention.ms", "2592000000", // 30 days retention cho alerts
                "compression.type", "snappy"
            ));
    }

    /**
     * ETL Pipeline Status Topic
     */
    @Bean
    public NewTopic etlPipelineStatusTopic() {
        return new NewTopic("etl-pipeline-status", 1, (short) 1)
            .configs(Map.of(
                "retention.ms", "259200000", // 3 days retention
                "compression.type", "snappy"
            ));
    }
}
