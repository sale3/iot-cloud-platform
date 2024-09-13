package etf.iot.cloud.platform.services.dto;

import lombok.*;
import lombok.Data;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StartupFetchingMessage {

    private String type;
    private List<Protocol> protocols;

}
