package net.unit8.metrics.mackerel;

import com.codahale.metrics.MetricRegistry;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class MackerelReporterTest {
    @Test(expected = IllegalArgumentException.class)
    public void test() {
        MackerelSender sender = new MackerelSender("test", "test", "http://localhost:8080");
        MetricRegistry registry = new MetricRegistry();
        MackerelReporter reporter = MackerelReporter.forRegistry(registry).build(sender);
        reporter.start(10, TimeUnit.SECONDS);
    }
}
