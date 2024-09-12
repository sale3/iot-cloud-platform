package etf.iot.cloud.platform.services.dto;

import lombok.*;
import lombok.Data;

import java.util.List;

/**
 * Class representing message containing protocol and protocol data sent from cloud to gateway via MQTT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GatewayProtocolMessage {
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
    private List<Protocol> protocols;

}
