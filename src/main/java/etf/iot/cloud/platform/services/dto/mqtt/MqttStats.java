package etf.iot.cloud.platform.services.dto.mqtt;

import etf.iot.cloud.platform.services.dto.Stats;

public class MqttStats {
    /**
    * Username of gateway device that sent sensor data stats via mqtt
    */
    public String username;
    /**
    * Sensor stats sent by gateway
    */
    public Stats payload;
}
