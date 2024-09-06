package etf.iot.cloud.platform.services.model;

import etf.iot.cloud.platform.services.enums.DataAggregationMethod;
import etf.iot.cloud.platform.services.enums.DataMode;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class ProtocolDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long canId;

    @Column(nullable = false)
    private int startBit;

    @Column(nullable = false)
    private int numBits;

    @Column(nullable = false)
    private int transmitInterval;

    @Enumerated(EnumType.STRING)
    private DataAggregationMethod aggregationMethod;

    @Enumerated(EnumType.STRING)
    private DataMode mode;

    @Column(nullable = false)
    private int multiplier;

    @Column(nullable = false)
    private int divisor;

    @Column(nullable = false)
    private int offsetValue;

    @Column(nullable = false)
    private boolean assigned = false;

    private String unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="protocol",referencedColumnName = "id", nullable = false)
    private ProtocolEntity protocol;

}
