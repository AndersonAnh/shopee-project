package vn.shopee.shopeeproject.db.kafka.checkKafka;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
class KafkaContainerSmokeTest {

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Test
    void starts() {
        System.out.println("BOOTSTRAP: " + KAFKA.getBootstrapServers());
        assertThat(KAFKA.isRunning()).isTrue();
    }
}