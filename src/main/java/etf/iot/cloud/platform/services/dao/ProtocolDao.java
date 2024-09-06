package etf.iot.cloud.platform.services.dao;

import etf.iot.cloud.platform.services.model.ProtocolDataEntity;
import etf.iot.cloud.platform.services.model.ProtocolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProtocolDao extends JpaRepository<ProtocolEntity, Long> {


    List<ProtocolEntity> findByAssigned(boolean assigned);

}
