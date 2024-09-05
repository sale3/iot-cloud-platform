package etf.iot.cloud.platform.services.services;

import etf.iot.cloud.platform.services.dto.OperationResult;
import etf.iot.cloud.platform.services.dto.ProtocolData;
import etf.iot.cloud.platform.services.dto.ProtocolDataSubmission;
import etf.iot.cloud.platform.services.exceptions.EntityNotPresentException;

import java.util.List;

public interface ProtocolDataService {

    List<ProtocolData> getByProtocolId(long protocolId);

    OperationResult submitProtocolDataChanges(long protocolId, ProtocolDataSubmission protocolDataSubmission) throws EntityNotPresentException;
}
