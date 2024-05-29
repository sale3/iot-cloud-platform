package etf.iot.cloud.platform.services.services;

import etf.iot.cloud.platform.services.dto.Data;
import etf.iot.cloud.platform.services.dto.DeviceData;

/**
 * Interface for sensor data service
 */
public interface DataService {
    /**
     * Processes and persists received sensor data
     *
     * @param data sensor data object
     */
    void receive(Data data);

    /**
     * Processes and persists received sensor data via mqtt broker
     *
     * @param username name of the gateway device that sent data via mqtt broker
     * @param data sensor data object
     */
    void receiveMqtt(String username, Data data);

    /**
     * Returns sensor and stats data produced by specified iot gateway
     *
     * @param id id of iot gateway
     * @return data
     */
    DeviceData deviceData(long id);
}
