package etf.iot.cloud.platform.services.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Class that represents sensor data.
 */
@lombok.Data
@Getter
@Setter
public class Data {
    /**
     * Sensor data value
     */
    private double value;
    /**
     * Sensor data generation time.
     */
    private String time;
    /**
     * Sensor data type.
     */
    private String type;
    /**
     * Sensor data measurement unit
     */
    private String unit;

    /**
     * Class constructor.
     */
    public Data(){}
}
