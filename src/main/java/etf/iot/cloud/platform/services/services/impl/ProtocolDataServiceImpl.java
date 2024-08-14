package etf.iot.cloud.platform.services.services.impl;

import etf.iot.cloud.platform.services.dao.ProtocolDao;
import etf.iot.cloud.platform.services.dao.ProtocolDataDao;
import etf.iot.cloud.platform.services.dto.OperationResult;
import etf.iot.cloud.platform.services.dto.ProtocolData;
import etf.iot.cloud.platform.services.dto.ProtocolDataSubmission;
import etf.iot.cloud.platform.services.exceptions.EntityNotPresentException;
import etf.iot.cloud.platform.services.model.ProtocolDataEntity;
import etf.iot.cloud.platform.services.model.ProtocolEntity;
import etf.iot.cloud.platform.services.services.ProtocolDataService;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;

    public ProtocolDataServiceImpl(ProtocolDataDao protocolDataDao, ProtocolDao protocolDao, ModelMapper modelMapper) {
        this.protocolDataDao = protocolDataDao;
        this.protocolDao = protocolDao;
        this.modelMapper = modelMapper;
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

    private ProtocolData mapProtocolDataEntity(ProtocolDataEntity protocolDataEntity) {
        return modelMapper.map(protocolDataEntity, ProtocolData.class);
    }

}
