package etf.iot.cloud.platform.services.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Locale;

@Data
@Getter
@Setter
public class GatewayIdentifiersMessage {
    /**
     * Type of the MQTT message sent.
     */
    private String type;
    /**
     * Action of the MQTT message sent.
     */
    private String action;
    /**
     * List of protocols which also contains protocol data.
     */
    private List<Long> protocols;
}
