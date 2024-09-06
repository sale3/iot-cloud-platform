package etf.iot.cloud.platform.services.dao;

import etf.iot.cloud.platform.services.model.ProtocolInputEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProtocolInputDao extends JpaRepository<ProtocolInputEntity, Long> {

    @Modifying
    @Query("DELETE FROM ProtocolInputEntity p WHERE p.dataId = :dataId")
    void deleteByDataId(Long dataId);

    @Modifying
    @Query("DELETE FROM ProtocolInputEntity p WHERE p.dataId IN :dataIds")
    void deleteByDataIdIn(List<Long> dataIds);
}
