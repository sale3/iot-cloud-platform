package etf.iot.cloud.platform.services.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@ToString
public class ProtocolStatsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long dataId;

    private double value;

    private Date time;

    private String unit;

    /*private Integer canId;

    private Integer startBit;

    private Integer numBits;*/

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="device",referencedColumnName = "id",nullable = false)
    private DeviceEntity device;

}
