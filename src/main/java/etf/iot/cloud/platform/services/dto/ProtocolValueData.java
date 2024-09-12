package etf.iot.cloud.platform.services.dto;

import lombok.*;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProtocolValueData {

    private Long id;
    private Long dataId;
    private Double value;

}
