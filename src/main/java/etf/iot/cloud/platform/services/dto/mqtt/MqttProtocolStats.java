package etf.iot.cloud.platform.services.dto.mqtt;

import etf.iot.cloud.platform.services.dto.ProtocolStats;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MqttProtocolStats {

    private String username;

    private ProtocolStats payload;

}
