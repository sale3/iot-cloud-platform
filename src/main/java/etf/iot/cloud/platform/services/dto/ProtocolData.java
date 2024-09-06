package etf.iot.cloud.platform.services.dto;

import etf.iot.cloud.platform.services.enums.DataAggregationMethod;
import etf.iot.cloud.platform.services.enums.DataMode;
import lombok.*;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolData {

    private long id;

    private String name;

    private long canId;

    private int startBit;

    private int numBits;

    private int transmitInterval;

    private DataAggregationMethod aggregationMethod;

    private DataMode mode;

    private int multiplier;

    private int divisor;

    private int offsetValue;

    private String unit;

    private boolean assigned;
}
