package net.unit8.metrics.mackerel;

import org.junit.Test;

public class MackerelSenderTest {
    @Test
    public void test() {
        MackerelSender sender = new MackerelSender("test", "test");
        sender.send("metric1", 10.0, System.currentTimeMillis());
        sender.flush();
        sender.send("metric1", 10.0, System.currentTimeMillis());
        sender.flush();
        sender.send("metric1", 10.0, System.currentTimeMillis());
        sender.flush();
        sender.send("metric1", 10.0, System.currentTimeMillis());
        sender.flush();
    }
}
