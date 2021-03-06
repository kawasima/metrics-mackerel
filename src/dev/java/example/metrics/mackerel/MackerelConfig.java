package example.metrics.mackerel;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import net.unit8.metrics.mackerel.MackerelReporter;
import net.unit8.metrics.mackerel.MackerelSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.SystemPublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Configuration
@ConfigurationProperties(prefix = "mackerel")
public class MackerelConfig {
    @Autowired
    private MetricRegistry registry;

    @Autowired
    private SystemPublicMetrics systemPublicMetrics;

    private String apiKey;
    private String serviceName;

    @PostConstruct
    public void initialize() {
        for (Metric<?> metric : systemPublicMetrics.metrics()) {
            Gauge<Long> metricGauge = () -> metric.getValue().longValue();
            String name = metric.getName();
            if (!name.contains(".")) {
                name += ".total";
            }
            registry.register(name, metricGauge);
        }

        final MackerelSender sender = new MackerelSender(serviceName, apiKey);
        final MackerelReporter reporter = MackerelReporter
                .forRegistry(registry)
                .build(sender);
        reporter.start(5, TimeUnit.MINUTES);
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
