package etf.iot.cloud.platform.services.dto;

import lombok.*;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProtocolInputStopData {

    private String type;
    private String action;
    private Long dataId;

}
