package net.unit8.metrics.mackerel;

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

    public MackerelSender(String serviceName, String apiKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://mackerel.io")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

         apiService = retrofit.create(MackerelApiService.class);
         metrics = new ArrayList<MackerelServiceMetric>();
         this.serviceName = serviceName;
         this.apiKey = apiKey;
    }

    /**
     * Sends the given measurement to the server.
     *
     * @param name         the name of the metric
     * @param value        the value of the metric
     * @param timestamp    the timestamp of the metric
     */
    public void send(String name, Double value, long timestamp) {
        metrics.add(new MackerelServiceMetric(name, value, timestamp));
    }

    /**
     * Flushes buffer, if applicable
     *
     * @throws IOException if Mackerel returns other than 200
     */
    void flush() throws IOException {
        Response response = apiService.postServiceMetrics(serviceName, apiKey, metrics).execute();
        if (response.code() != 200) {
            throw new IOException("Fail to send a Mackerel server.");
        }
    }
}
