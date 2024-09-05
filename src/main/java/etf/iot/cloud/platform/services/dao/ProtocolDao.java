package etf.iot.cloud.platform.services.dao;

import etf.iot.cloud.platform.services.model.ProtocolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProtocolDao extends JpaRepository<ProtocolEntity, Long> {



}
