package com.ninjaone.dundie_awards.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
public class BaseIntegrationTest {

    private static final int REDIS_PORT = 6379;
    private static final int ELASTICMQ_PORT = 9324;

    private static final String REDIS_IMAGE = "redis:7.4.2";
    private static final String ELASTICMQ_IMAGE = "softwaremill/elasticmq-native:1.6.12";

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
            .withExposedPorts(REDIS_PORT);

    @Container
    static GenericContainer<?> elasticMQContainer = new GenericContainer<>(DockerImageName.parse(ELASTICMQ_IMAGE))
            .withExposedPorts(ELASTICMQ_PORT);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", () -> "localhost");
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(REDIS_PORT));
        registry.add("spring.cloud.aws.sqs.endpoint", () ->
                "http://localhost:" + elasticMQContainer.getMappedPort(ELASTICMQ_PORT));
    }
}
