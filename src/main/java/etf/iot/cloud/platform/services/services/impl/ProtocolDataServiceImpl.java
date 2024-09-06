package etf.iot.cloud.platform.services.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import etf.iot.cloud.platform.services.dao.ProtocolDao;
import etf.iot.cloud.platform.services.dao.ProtocolDataDao;
import etf.iot.cloud.platform.services.dao.ProtocolInputDao;
import etf.iot.cloud.platform.services.dto.*;
import etf.iot.cloud.platform.services.exceptions.EntityNotPresentException;
import etf.iot.cloud.platform.services.model.ProtocolDataEntity;
import etf.iot.cloud.platform.services.model.ProtocolEntity;
import etf.iot.cloud.platform.services.model.ProtocolInputEntity;
import etf.iot.cloud.platform.services.mqtt.MqttPublisher;
import etf.iot.cloud.platform.services.services.ProtocolDataService;
import jakarta.transaction.Transactional;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProtocolDataServiceImpl implements ProtocolDataService {

    private final ProtocolDataDao protocolDataDao;
    private final ProtocolDao protocolDao;
    private final ProtocolInputDao protocolInputDao;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final MqttPublisher mqttPublisher;

    @Value("${mqtt.PROTOCOL_TOPIC}")
    private String PROTOCOL_TOPIC;

    public ProtocolDataServiceImpl(ProtocolDataDao protocolDataDao, ProtocolDao protocolDao, ProtocolInputDao protocolInputDao, ModelMapper modelMapper, ObjectMapper objectMapper, MqttPublisher mqttPublisher) {
        this.protocolDataDao = protocolDataDao;
        this.protocolDao = protocolDao;
        this.protocolInputDao = protocolInputDao;
        this.modelMapper = modelMapper;
        this.objectMapper = objectMapper;
        this.mqttPublisher = mqttPublisher;
    }

    @Override
    public List<ProtocolData> getByProtocolId(long protocolId) {
        List<ProtocolDataEntity> protocolDataEntities = protocolDataDao.findByProtocolId(protocolId);
        List<ProtocolData> protocolDataList = protocolDataEntities.stream().map(this::mapProtocolDataEntity).toList();
        return protocolDataList.stream().sorted(Comparator.comparingLong(ProtocolData::getId)).toList();
    }

    @Override
    public OperationResult submitProtocolDataChanges(long protocolId, ProtocolDataSubmission protocolDataSubmission) throws EntityNotPresentException {
        ProtocolEntity protocolEntity = protocolDao.findById(protocolId).orElseThrow(EntityNotPresentException::new);
        List<ProtocolData> protocolData = protocolDataSubmission.getProtocolData();
        List<ProtocolDataEntity> protocolDataEntities = protocolDataDao.findByProtocolId(protocolId);
        Set<Long> protocolIds = protocolData.stream().map(ProtocolData::getId).collect(Collectors.toSet());
        List<ProtocolDataEntity> entitiesToRemove = protocolDataEntities.stream()
                .filter(entity -> !protocolIds.contains(entity.getId()))
                .toList();

        // Update protocol name if changed
        if(!protocolEntity.getName().equals(protocolDataSubmission.getProtocolName())) {
            protocolEntity.setName(protocolDataSubmission.getProtocolName());
            protocolDao.save(protocolEntity);
        }

        // Delete relevant protocol data
        if(!entitiesToRemove.isEmpty()) {
            protocolDataDao.deleteAll(entitiesToRemove);
        }

        List<ProtocolData> newProtocols = protocolData.stream().filter(p -> p.getId() == 0).toList();
        List<ProtocolData> existingProtocols = protocolData.stream().filter(p -> p.getId() != 0).toList();

        // Insert new protocol data
        for (ProtocolData newProtocolData : newProtocols) {
            ProtocolDataEntity newEntity = new ProtocolDataEntity();
            newEntity.setName(newProtocolData.getName());
            newEntity.setMode(newProtocolData.getMode());
            newEntity.setDivisor(newProtocolData.getDivisor());
            newEntity.setMultiplier(newProtocolData.getMultiplier());
            newEntity.setCanId(newProtocolData.getCanId());
            newEntity.setUnit(newProtocolData.getUnit());
            newEntity.setAggregationMethod(newProtocolData.getAggregationMethod());
            newEntity.setNumBits(newProtocolData.getNumBits());
            newEntity.setOffsetValue(newProtocolData.getOffsetValue());
            newEntity.setStartBit(newProtocolData.getStartBit());
            newEntity.setTransmitInterval(newProtocolData.getTransmitInterval());
            newEntity.setProtocol(protocolEntity);
            protocolDataDao.save(newEntity);
        }

        // Update existing protocol data
        for (ProtocolData updatedProtocolData : existingProtocols) {
            Optional<ProtocolDataEntity> existingEntityOptional = protocolDataDao.findById(updatedProtocolData.getId());
            if(existingEntityOptional.isPresent()) {
                ProtocolDataEntity existingEntity = existingEntityOptional.get();
                boolean isUpdated = false;

                // Check and update each field
                if (!existingEntity.getName().equals(updatedProtocolData.getName())) {
                    existingEntity.setName(updatedProtocolData.getName());
                    isUpdated = true;
                }
                if (!existingEntity.getMode().equals(updatedProtocolData.getMode())) {
                    existingEntity.setMode(updatedProtocolData.getMode());
                    isUpdated = true;
                }
                if (existingEntity.getDivisor() != updatedProtocolData.getDivisor()) {
                    existingEntity.setDivisor(updatedProtocolData.getDivisor());
                    isUpdated = true;
                }
                if (existingEntity.getMultiplier() != updatedProtocolData.getMultiplier()) {
                    existingEntity.setMultiplier(updatedProtocolData.getMultiplier());
                    isUpdated = true;
                }
                if (existingEntity.getCanId() != updatedProtocolData.getCanId()) {
                    existingEntity.setCanId(updatedProtocolData.getCanId());
                    isUpdated = true;
                }
                if (existingEntity.getUnit() != null && updatedProtocolData.getUnit() != null && !existingEntity.getUnit().equals(updatedProtocolData.getUnit())) {
                    existingEntity.setUnit(updatedProtocolData.getUnit());
                    isUpdated = true;
                }
                if (!existingEntity.getAggregationMethod().equals(updatedProtocolData.getAggregationMethod())) {
                    existingEntity.setAggregationMethod(updatedProtocolData.getAggregationMethod());
                    isUpdated = true;
                }
                if (existingEntity.getNumBits() != updatedProtocolData.getNumBits()) {
                    existingEntity.setNumBits(updatedProtocolData.getNumBits());
                    isUpdated = true;
                }
                if (existingEntity.getOffsetValue() != updatedProtocolData.getOffsetValue()) {
                    existingEntity.setOffsetValue(updatedProtocolData.getOffsetValue());
                    isUpdated = true;
                }
                if (existingEntity.getStartBit() != updatedProtocolData.getStartBit()) {
                    existingEntity.setStartBit(updatedProtocolData.getStartBit());
                    isUpdated = true;
                }
                if (existingEntity.getTransmitInterval() != updatedProtocolData.getTransmitInterval()) {
                    existingEntity.setTransmitInterval(updatedProtocolData.getTransmitInterval());
                    isUpdated = true;
                }

                if (isUpdated) {
                    protocolDataDao.save(existingEntity);
                }
            }
        }

        return new OperationResult(true);
    }

    @Override
    public void sendDataToDevice(ProtocolInputSetData protocolInputData) {
        try {
            System.out.println(protocolInputData.getDataId());
            System.out.println("Type = " + protocolInputData.getType());
            System.out.println("Action = " + protocolInputData.getAction());
            String jsonPayload = objectMapper.writeValueAsString(protocolInputData);
            mqttPublisher.publish(PROTOCOL_TOPIC, jsonPayload);
        } catch (JsonProcessingException | MqttException e) {
            System.out.println("Exception = " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void stopDataFromDevice(ProtocolInputStopData protocolInputData) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(protocolInputData);
            mqttPublisher.publish(PROTOCOL_TOPIC, jsonPayload);
            protocolInputDao.deleteByDataId(protocolInputData.getDataId());
        } catch (JsonProcessingException | MqttException e) {
            System.out.println("Exception = " + e.getMessage());
        }
    }

    @Override
    public void protocolValueDataGatewayRequest(){
        try {
            ProtocolValueInputData protocolValueInputData = new ProtocolValueInputData();
            protocolValueInputData.setType("can_message");
            protocolValueInputData.setAction("get_current_values");
            String jsonPayload = objectMapper.writeValueAsString(protocolValueInputData);
            mqttPublisher.publish(PROTOCOL_TOPIC, jsonPayload);
            System.out.println("Request sent!!!");
        } catch (JsonProcessingException | MqttException e) {
            System.out.println("Exception = " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void writeDataToDatabase(List<ProtocolValueData> protocolValueData) {
        List<Long> dataIds = protocolValueData.stream()
                .map(ProtocolValueData::getDataId)
                .collect(Collectors.toList());
        protocolInputDao.deleteByDataIdIn(dataIds);
        List<ProtocolInputEntity> protocolInputEntities = protocolValueData.stream().map(this::mapProtocolValueData).toList();
        protocolInputDao.saveAll(protocolInputEntities);
    }

    @Override
    public List<ProtocolValueData> getProtocolValueData() {
        List<ProtocolInputEntity> protocolInputEntities = protocolInputDao.findAll();
        return protocolInputEntities.stream().map(this::mapProtocolInputEntity).toList();
    }

    @Override
    public ProtocolDataEntity findById(Long id) {
        return protocolDataDao.findById(id).get();
    }

    private ProtocolValueData mapProtocolInputEntity(ProtocolInputEntity protocolInputEntity) {
        return modelMapper.map(protocolInputEntity, ProtocolValueData.class);
    }

    private ProtocolInputEntity mapProtocolValueData(ProtocolValueData protocolValueData) {
        return modelMapper.map(protocolValueData, ProtocolInputEntity.class);
    }

    private ProtocolData mapProtocolDataEntity(ProtocolDataEntity protocolDataEntity) {
        return modelMapper.map(protocolDataEntity, ProtocolData.class);
    }

}
