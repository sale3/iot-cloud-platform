package etf.iot.cloud.platform.services.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
public class ProtocolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "protocol", cascade = CascadeType.ALL)
    private List<ProtocolDataEntity> protocolData;

    @Column(nullable = false)
    private boolean assigned = false;

}
