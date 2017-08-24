package example.metrics.mackerel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class MackerelMetricsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MackerelMetricsApplication.class, args);
    }
}
