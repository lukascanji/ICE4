package ca.gbc.productservice;
import org.jetbrains.annotations.NotNull;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.MongoDBContainer;

public class AbstractContainerBaseTest {

    static final MongoDBContainer mongoDBContainer;

    static {
        mongoDBContainer = new MongoDBContainer("mongo:4.0.10");
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void mongoDBProperties(@NotNull DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
}
