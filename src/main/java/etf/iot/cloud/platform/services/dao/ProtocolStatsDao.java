package etf.iot.cloud.platform.services.dao;

import etf.iot.cloud.platform.services.model.ProtocolStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProtocolStatsDao extends JpaRepository<ProtocolStatsEntity, Long> {
}
