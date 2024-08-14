package etf.iot.cloud.platform.services.services;

import etf.iot.cloud.platform.services.dto.OperationResult;
import etf.iot.cloud.platform.services.dto.Protocol;
import etf.iot.cloud.platform.services.exceptions.EntityNotPresentException;

import java.util.List;

public interface ProtocolService {

    List<Protocol> getAll();

    Protocol getById(long id) throws EntityNotPresentException;

    OperationResult checkIfProtocolWithTheSameNameExists(Protocol protocol);

    Protocol createProtocol(Protocol protocol);

    OperationResult deleteProtocol(long id);
}
