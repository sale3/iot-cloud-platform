package etf.iot.cloud.platform.services.dao;

import etf.iot.cloud.platform.services.model.ProtocolDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProtocolDataDao extends JpaRepository<ProtocolDataEntity, Long> {

    @Query("SELECT p FROM ProtocolDataEntity p WHERE p.protocol.id = :protocolId")
    List<ProtocolDataEntity> findByProtocolId(@Param("protocolId") Long protocolId);


}
