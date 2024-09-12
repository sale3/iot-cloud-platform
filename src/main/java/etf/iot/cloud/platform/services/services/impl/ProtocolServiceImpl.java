package etf.iot.cloud.platform.services.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import etf.iot.cloud.platform.services.dao.ProtocolDao;
import etf.iot.cloud.platform.services.dao.ProtocolDataDao;
import etf.iot.cloud.platform.services.dto.*;
import etf.iot.cloud.platform.services.exceptions.EntityNotPresentException;
import etf.iot.cloud.platform.services.model.ProtocolDataEntity;
import etf.iot.cloud.platform.services.model.ProtocolEntity;
import etf.iot.cloud.platform.services.mqtt.MqttPublisher;
import etf.iot.cloud.platform.services.services.ProtocolService;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ProtocolServiceImpl implements ProtocolService {

    private final ProtocolDao protocolDao;
    private final ProtocolDataDao protocolDataDao;
    private final ModelMapper modelMapper;
    private final MqttPublisher mqttPublisher;
    private final ObjectMapper objectMapper;

    @Value("${mqtt.PROTOCOL_TOPIC}")
    private String PROTOCOL_TOPIC;

    @Value("${mqtt.PROTOCOL_STARTUP_TOPIC}")
    private String PROTOCOL_STARTUP_TOPIC;

    public ProtocolServiceImpl(ProtocolDao protocolDao, ProtocolDataDao protocolDataDao, ModelMapper modelMapper, MqttPublisher mqttPublisher, ObjectMapper objectMapper) {
        this.protocolDao = protocolDao;
        this.protocolDataDao = protocolDataDao;
        this.modelMapper = modelMapper;
        this.mqttPublisher = mqttPublisher;
        this.objectMapper = objectMapper;
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

    @Override
    public OperationResult protocolAssignment(List<Protocol> protocols) {
        List<ProtocolEntity> databaseProtocols = protocolDao.findAll();
        List<Protocol> addedProtocols = new ArrayList<>();
        List<Protocol> removedProtocols = new ArrayList<>();

        for (Protocol protocol : protocols) {
            for(ProtocolEntity protocolEntity : databaseProtocols) {
                if(protocol.getId() == protocolEntity.getId() && protocol.isAssigned() != protocolEntity.isAssigned()) {
                    List<ProtocolDataEntity> protocolDataEntities = protocolDataDao.findByProtocolId(protocolEntity.getId());
                    if(protocol.isAssigned()) {
                        for(ProtocolDataEntity protocolDataEntity : protocolDataEntities) {
                            protocolDataEntity.setAssigned(true);
                        }
                        List<ProtocolData> protocolData = protocolDataEntities.stream().map(this::mapProtocolDataEntity).toList();
                        protocol.setProtocolData(protocolData);
                        addedProtocols.add(protocol);
                    } else {
                        for(ProtocolDataEntity protocolDataEntity : protocolDataEntities) {
                            protocolDataEntity.setAssigned(false);
                        }
                        protocol.setProtocolData(new ArrayList<>());
                        removedProtocols.add(protocol);
                    }
                    protocolEntity.setAssigned(protocol.isAssigned());
                    protocolDataDao.saveAll(protocolDataEntities);
                    protocolDao.save(protocolEntity);
                }
            }
        }

        //Send added protocols
        if(addedProtocols.size() > 0) {
            try {
                GatewayProtocolMessage gatewayProtocolAssignmentMessage = new GatewayProtocolMessage();
                gatewayProtocolAssignmentMessage.setType("protocol_assignment");
                gatewayProtocolAssignmentMessage.setAction("add");
                gatewayProtocolAssignmentMessage.setProtocols(addedProtocols);
                String jsonPayload = objectMapper.writeValueAsString(gatewayProtocolAssignmentMessage);
                mqttPublisher.publish(PROTOCOL_TOPIC, jsonPayload);
            } catch (JsonProcessingException | MqttException e) {
                return new OperationResult(false);
            }
        }

        //Send removed protocols
        if(removedProtocols.size() > 0) {
            try {
                GatewayProtocolMessage gatewayProtocolAssignmentMessage = new GatewayProtocolMessage();
                gatewayProtocolAssignmentMessage.setType("protocol_assignment");
                gatewayProtocolAssignmentMessage.setAction("remove");
                gatewayProtocolAssignmentMessage.setProtocols(removedProtocols);
                String jsonPayload = objectMapper.writeValueAsString(gatewayProtocolAssignmentMessage);
                mqttPublisher.publish(PROTOCOL_TOPIC, jsonPayload);
            } catch (JsonProcessingException | MqttException e) {
                return new OperationResult(false);
            }
        }

        return new OperationResult(true);
    }

    @Override
    public void returnProtocolStartupData() {
        List<ProtocolEntity> protocolEntities = protocolDao.findByAssigned(true);
        List<Protocol> assignedProtocols = new ArrayList<>();

        for(ProtocolEntity protocolEntity : protocolEntities) {
            List<ProtocolDataEntity> protocolDataEntities = protocolDataDao.findByProtocolId(protocolEntity.getId());
            for (ProtocolDataEntity protocolDataEntity : protocolDataEntities) {
                protocolDataEntity.setAssigned(true);
            }
            List<ProtocolData> protocolData = protocolDataEntities.stream().map(this::mapProtocolDataEntity).toList();
            Protocol protocol = new Protocol();
            protocol.setProtocolData(protocolData);
            protocol.setAssigned(protocolEntity.isAssigned());
            protocol.setId(protocolEntity.getId());
            protocol.setName(protocolEntity.getName());
            assignedProtocols.add(protocol);
            protocolDataDao.saveAll(protocolDataEntities);
        }

        if(assignedProtocols.size() > 0) {
            try {
                GatewayProtocolMessage gatewayProtocolAssignmentMessage = new GatewayProtocolMessage();
                gatewayProtocolAssignmentMessage.setType("startup_fetching");
                gatewayProtocolAssignmentMessage.setAction("add");
                gatewayProtocolAssignmentMessage.setProtocols(assignedProtocols);
                String jsonPayload = objectMapper.writeValueAsString(gatewayProtocolAssignmentMessage);
                mqttPublisher.publish(PROTOCOL_TOPIC, jsonPayload);
            } catch (JsonProcessingException | MqttException e) {
                System.out.println("Exception = " + e.getMessage());
            }
        }

    }

    @Override
    public OperationResult syncProtocols() {
        try {
            List<ProtocolEntity> assignedProtocols = protocolDao.findAll().stream().filter(ProtocolEntity::isAssigned).toList();
            for(ProtocolEntity protocolEntity : assignedProtocols) {
                protocolEntity.setAssigned(true);
            }
            protocolDao.saveAll(assignedProtocols);
            SyncMessage syncMessage = new SyncMessage();
            syncMessage.setType("sync");
            String jsonPayload = objectMapper.writeValueAsString(syncMessage);
            mqttPublisher.publish(PROTOCOL_TOPIC, jsonPayload);
            return new OperationResult(true);
        } catch (JsonProcessingException | MqttException e) {
            System.out.println("Exception = " + e.getMessage());
        }
        return null;
    }

    private Protocol mapProtocolEntity(ProtocolEntity protocolEntity) {
        return modelMapper.map(protocolEntity, Protocol.class);
    }

    private ProtocolData mapProtocolDataEntity(ProtocolDataEntity protocolDataEntity) {
        return modelMapper.map(protocolDataEntity, ProtocolData.class);
    }

}
