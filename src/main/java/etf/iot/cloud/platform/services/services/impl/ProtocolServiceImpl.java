package etf.iot.cloud.platform.services.services.impl;

import etf.iot.cloud.platform.services.dao.ProtocolDao;
import etf.iot.cloud.platform.services.dto.OperationResult;
import etf.iot.cloud.platform.services.dto.Protocol;
import etf.iot.cloud.platform.services.exceptions.EntityNotPresentException;
import etf.iot.cloud.platform.services.model.ProtocolEntity;
import etf.iot.cloud.platform.services.mqtt.MqttPublisher;
import etf.iot.cloud.platform.services.services.ProtocolService;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ProtocolServiceImpl implements ProtocolService {

    private final ProtocolDao protocolDao;
    private final ModelMapper modelMapper;
    private final MqttPublisher mqttPublisher;

    @Value("${mqtt.PROTOCOL_TOPIC}")
    private String PROTOCOL_TOPIC;

    public ProtocolServiceImpl(ProtocolDao protocolDao, ModelMapper modelMapper, MqttPublisher mqttPublisher) {
        this.protocolDao = protocolDao;
        this.modelMapper = modelMapper;
        this.mqttPublisher = mqttPublisher;
    }

    @Override
    public List<Protocol> getAll() {
        List<ProtocolEntity> protocolEntities = protocolDao.findAll();
        List<Protocol> protocols = protocolEntities.stream().map(this::mapProtocolEntity).toList();
        return protocols.stream().sorted(Comparator.comparingLong(Protocol::getId)).toList();
    }

    @Override
    public Protocol getById(long id) throws EntityNotPresentException {
        ProtocolEntity protocolEntity = protocolDao.findById(id).orElseThrow(EntityNotPresentException::new);
        return mapProtocolEntity(protocolEntity);
    }

    @Override
    public OperationResult checkIfProtocolWithTheSameNameExists(Protocol protocol) {
        List<ProtocolEntity> protocolEntities = protocolDao.findAll();
        Optional<ProtocolEntity> foundProtocol = protocolDao.findById(protocol.getId());
        if(foundProtocol.isPresent()) {
            boolean found = false;
            for(ProtocolEntity protocolEntity1 : protocolEntities) {
                if(protocolEntity1.getId() != protocol.getId() && protocolEntity1.getName().equals(protocol.getName())) {
                    found = true;
                }
            }
            if(found) {
                return new OperationResult(false);
            } else {
                return new OperationResult(true);
            }
        }
        return new OperationResult(false);
    }

    @Override
    public Protocol createProtocol(Protocol protocol) {
        ProtocolEntity protocolEntity = new ProtocolEntity();
        protocolEntity.setName(protocol.getName());
        ProtocolEntity savedProtocol = protocolDao.save(protocolEntity);

        try {
            mqttPublisher.publish("Sent message successfully");
            System.out.println("Message sent!");
        } catch (MqttException e) {
            System.out.println("Error sending MQTT message!");
            throw new RuntimeException(e);
        }

        return mapProtocolEntity(savedProtocol);
    }

    @Override
    public OperationResult deleteProtocol(long id) {
        Optional<ProtocolEntity> protocolEntity = protocolDao.findById(id);
        if(!protocolEntity.isPresent()) {
            return new OperationResult(false);
        }
        protocolDao.delete(protocolEntity.get());
        return new OperationResult(true);
    }

    private Protocol mapProtocolEntity(ProtocolEntity protocolEntity) {
        return modelMapper.map(protocolEntity, Protocol.class);
    }

}
