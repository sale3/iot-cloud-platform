package etf.iot.cloud.platform.services.services;

import etf.iot.cloud.platform.services.dto.*;
import etf.iot.cloud.platform.services.exceptions.EntityNotPresentException;
import etf.iot.cloud.platform.services.model.ProtocolDataEntity;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface ProtocolDataService {

    List<ProtocolData> getByProtocolId(long protocolId);

    OperationResult submitProtocolDataChanges(long protocolId, ProtocolDataSubmission protocolDataSubmission) throws EntityNotPresentException;

    void sendDataToDevice(ProtocolInputSetData protocolInputData);


    void stopDataFromDevice(ProtocolInputStopData protocolInputData);

    void protocolValueDataGatewayRequest();

    void writeDataToDatabase(List<ProtocolValueData> protocolValueData);

    List<ProtocolValueData> getProtocolValueData();

    ProtocolDataEntity findById(Long id);
}
