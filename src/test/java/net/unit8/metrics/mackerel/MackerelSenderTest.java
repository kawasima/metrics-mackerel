package net.unit8.metrics.mackerel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.Undertow;
import io.undertow.util.Headers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class MackerelSenderTest {
    Undertow undertow;
    ObjectMapper mapper = new ObjectMapper();
    int port;
    int handleCount;
    List<String> requestBodyList = new ArrayList<>();

    private int findPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }

    @Before
    public void setupServer() throws IOException {
        handleCount = 0;
        requestBodyList.clear();
        port = findPort();
        undertow = Undertow.builder()
                .setHandler(exchange -> {
                    handleCount += 1;
                    exchange.getRequestReceiver().receiveFullString((e, data) -> {
                        requestBodyList.add(data);
                    });
                    exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/plain");
                    exchange.setStatusCode(500);
                    exchange.getResponseSender().send("error");
                })
                .addHttpListener(port, "localhost")
                .build();
        undertow.start();
    }

    @Test
    public void test() {
        MackerelSender sender = new MackerelSender("test", "test", "http://localhost:" + port);
        try {
            sender.send("metric1", 10.0, System.currentTimeMillis());
            sender.flush();
        } catch (Exception ignore) {
        }
        try {
            sender.send("metric1", 10.0, System.currentTimeMillis());
            sender.flush();
        } catch (Exception ignore) {
        }
        try {
            sender.send("metric1", 10.0, System.currentTimeMillis());
            sender.flush();
        } catch (Exception ignore) {
        }
        try {
            sender.send("metric1", 10.0, System.currentTimeMillis());
            sender.flush();
            fail("Exception must occur");
        } catch (Exception e) {
            requestBodyList.forEach(body -> {
                try {
                    List<MackerelServiceMetric> metric = mapper.readValue(body, new TypeReference<List<MackerelServiceMetric>>() {});
                    assertThat(metric).hasSize(1);
                    assertThat(metric.get(0)).isEqualToComparingOnlyGivenFields(
                            new MackerelServiceMetric("metric1", 10.0, System.currentTimeMillis()),
                            "name", "value");
                } catch (Exception pe) {
                    fail("JSON parse error", pe);
                }
            });
            assert(handleCount == 3);
        }
    }

    @After
    public void tearDown() {
        if (undertow != null) {
            undertow.stop();
        }

    }
}
