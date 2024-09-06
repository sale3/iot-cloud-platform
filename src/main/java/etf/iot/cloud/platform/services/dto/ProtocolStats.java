package etf.iot.cloud.platform.services.dto;

import lombok.*;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProtocolStats {

    private long dataId;

    private double value;

    private String time;

    private String unit;

}
