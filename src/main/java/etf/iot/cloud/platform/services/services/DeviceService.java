package etf.iot.cloud.platform.services.services;

import etf.iot.cloud.platform.services.dto.Device;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Interface for iot gateway app/device account service
 */
public interface DeviceService extends UserDetailsService {
    /**
     * Creates account for new iot gateway app
     *
     * @param device iot gateway app/device data
     * @return device entity
     */
    Device createDevice(Device device);

    /**
     * Grab device that has specified username
     *
     * @param username Username of the device
     * @return device entity
     */
    Device loadUserByUsername(String username) throws UsernameNotFoundException;
}
