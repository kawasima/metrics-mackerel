package net.unit8.metrics.mackerel;

import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.Failsafe;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MackerelSender {
    private final MackerelApiService apiService;
    private final List<MackerelServiceMetric> metrics;
    private final String serviceName;
    private final String apiKey;
    private final CircuitBreaker circuitBreaker;
    private String userAgent;

    public MackerelSender(String serviceName, String apiKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://mackerel.io")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        circuitBreaker = new CircuitBreaker().failOn(IOException.class)
                .withFailureThreshold(3)
                .withSuccessThreshold(3);

        apiService = retrofit.create(MackerelApiService.class);
        metrics = new ArrayList<MackerelServiceMetric>();
        userAgent = "metrics-mackerel-for-" + serviceName;
        this.serviceName = serviceName;
        this.apiKey = apiKey;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Sends the given measurement to the server.
     *
     * @param name      the name of the metric
     * @param value     the value of the metric
     * @param timestamp the timestamp of the metric
     */
    public void send(String name, Double value, long timestamp) {
        metrics.add(new MackerelServiceMetric(name, value, timestamp));
    }

    /**
     * Flushes buffer, if applicable
     *
     */
    void flush() {
        try {
            Failsafe.with(circuitBreaker)
                    .run(() -> {
                        Response response = apiService.postServiceMetrics(
                                serviceName,
                                apiKey,
                                userAgent,
                                metrics).execute();
                        if (response.code() != 200) {
                            throw new IOException("Fail to send a Mackerel server.");
                        }
                    });
        } finally {
            metrics.clear();
        }
    }
}
