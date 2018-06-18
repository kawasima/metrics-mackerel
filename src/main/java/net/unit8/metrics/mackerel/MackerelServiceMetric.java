package net.unit8.metrics.mackerel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class MackerelServiceMetric implements Serializable {
    private String name;
    private long time;
    private Double value;

    @JsonCreator
    public MackerelServiceMetric(
            @JsonProperty("name")  String name,
            @JsonProperty("value") Double value,
            @JsonProperty("time")  long time) {
        this.name = name;
        this.value = value;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "MackerelServiceMetric{" +
                "name='" + name + '\'' +
                ", time=" + time +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MackerelServiceMetric that = (MackerelServiceMetric) o;

        return time == that.time && (name != null ? name.equals(that.name) : that.name == null) && (value != null ? value.equals(that.value) : that.value == null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
