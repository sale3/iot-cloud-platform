package etf.iot.cloud.platform.services.dto.mqtt;

import etf.iot.cloud.platform.services.dto.Data;

/**
 * Class that represents sensor data sent via mqtt
 */
public class MqttData {
    /**
    * Username of gateway device that sent sensor data via mqtt
    */
    public String username;

    /**
    * Sensor data sent by gateway
    */
    public Data payload;
}
