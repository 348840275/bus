package org.aoju.bus.gitlab.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.aoju.bus.gitlab.utils.JacksonJsonEnumHelper;

public enum HealthCheckStatus {
    OK, FAILED;

    private static JacksonJsonEnumHelper<HealthCheckStatus> enumHelper = new JacksonJsonEnumHelper<>(HealthCheckStatus.class);

    @JsonCreator
    public static HealthCheckStatus forValue(String value) {
        return enumHelper.forValue(value);
    }

    @JsonValue
    public String toValue() {
        return enumHelper.toString(this);
    }

    @Override
    public String toString() {
        return enumHelper.toString(this);
    }
}
