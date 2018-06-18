package net.unit8.metrics.mackerel;

import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.function.CheckedRunnable;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class MackerelSender {
    private static final String DEFAULT_BASE_URL = "https://api.mackerelio.com";
    private final MackerelApiService apiService;
    private final List<MackerelServiceMetric> metrics;
    private final String serviceName;
    private final String apiKey;
    private final CircuitBreaker circuitBreaker;
    private String userAgent;

    public MackerelSender(String serviceName, String apiKey) {
        this(serviceName, apiKey, DEFAULT_BASE_URL);
    }

    public MackerelSender(String serviceName, String apiKey, String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        InputStream resourceAsStream = this.getClass()
                .getResourceAsStream("/version.properties");
        Properties props = new Properties();
        try {
            props.load(resourceAsStream);
            userAgent = "metrics-mackerel/"
                    + props.getProperty("version")
                    + " (for " + serviceName + ")";
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if (resourceAsStream != null) {
                try {
                    resourceAsStream.close();
                } catch(IOException ignore) {
                }
            }
        }
        circuitBreaker = new CircuitBreaker()
                .withFailureThreshold(3)
                .withDelay(15, TimeUnit.MINUTES)
                .withSuccessThreshold(3);

        apiService = retrofit.create(MackerelApiService.class);
        metrics = new ArrayList<MackerelServiceMetric>();
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
                    .run(new CheckedRunnable() {
                        @Override
                        public void run() throws Exception {
                            Response response = apiService.postServiceMetrics(
                                    serviceName,
                                    apiKey,
                                    userAgent,
                                    metrics).execute();
                            if (response.code() != 200) {
                                throw new IOException("Fail to send a Mackerel server.");
                            }
                        }
                    });
        } finally {
            metrics.clear();
        }
    }
}
