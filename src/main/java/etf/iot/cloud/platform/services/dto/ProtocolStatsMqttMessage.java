package etf.iot.cloud.platform.services.dto;

import lombok.*;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProtocolStatsMqttMessage {

    private ProtocolStats protocolStats;

    private String protocolName;

    private String protocolDataName;

}
