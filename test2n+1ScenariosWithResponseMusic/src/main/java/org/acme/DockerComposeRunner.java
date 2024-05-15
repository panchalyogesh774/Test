package org.acme;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.File;
import java.time.Duration;

@ApplicationScoped
public class DockerComposeRunner {

    public static void runDockerComposeUp() {
        try {
            
            String composeFilePath = "./docker-compose.yml";
            DockerComposeContainer<?> container = new DockerComposeContainer<>(new File(composeFilePath))
            .withExposedService("elasticsearch", 9200, Wait.forHttp("/").forStatusCode(200).withStartupTimeout(Duration.ofSeconds(30)))

            .withExposedService("kibana", 5601, Wait.forHttp("/").forStatusCode(200).withStartupTimeout(Duration.ofSeconds(30)));

            
            container.start();
            System.out.println("Docker Compose Containers started successfully!");
            // container.stop();
        } catch (Exception e) {
            System.err.println("Error while starting Docker Compose Containers:");
            e.printStackTrace();
        }
    }

    
}
