package etf.iot.cloud.platform.services.dto;

import lombok.*;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Protocol {

    private long id;

    private String name;

    private boolean assigned;

}
