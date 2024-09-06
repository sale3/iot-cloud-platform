package etf.iot.cloud.platform.services.dto;

import lombok.*;
import lombok.Data;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Protocol {

    private long id;

    private String name;

    private List<ProtocolData> protocolData;

    private boolean assigned;

}
