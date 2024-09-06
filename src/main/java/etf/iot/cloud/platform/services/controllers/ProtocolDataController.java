package etf.iot.cloud.platform.services.controllers;

import etf.iot.cloud.platform.services.dto.*;
import etf.iot.cloud.platform.services.exceptions.EntityNotPresentException;
import etf.iot.cloud.platform.services.services.ProtocolDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/protocol_data")
public class ProtocolDataController {

    private final ProtocolDataService protocolDataService;

    public ProtocolDataController(ProtocolDataService protocolDataService) {
        this.protocolDataService = protocolDataService;
    }

    @GetMapping("/{protocolId}")
    public ResponseEntity<List<ProtocolData>> getByProtocolId(@PathVariable("protocolId") long protocolId) {
        return new ResponseEntity<>(protocolDataService.getByProtocolId(protocolId), HttpStatus.OK);
    }

    @PostMapping("/{protocolId}")
    public ResponseEntity<OperationResult> submitProtocolDataChanges
            (@PathVariable("protocolId") long protocolId, @RequestBody ProtocolDataSubmission protocolDataSubmission) throws EntityNotPresentException {
        return new ResponseEntity<>(protocolDataService.submitProtocolDataChanges(protocolId, protocolDataSubmission), HttpStatus.OK);
    }

    @PostMapping("/send_data_to_device")
    public ResponseEntity<Void> sendDataToDevice(@RequestBody ProtocolInputSetData protocolInputData) {
        protocolDataService.sendDataToDevice(protocolInputData);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stop_data_from_device")
    public ResponseEntity<Void> stopDataFromDevice(@RequestBody ProtocolInputStopData protocolInputData) {
        protocolDataService.stopDataFromDevice(protocolInputData);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/current_values_request")
    public ResponseEntity<Void> sendMQTTRequestForCurrentDataValues() {
        protocolDataService.protocolValueDataGatewayRequest();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/current_values")
    public ResponseEntity<List<ProtocolValueData>> getCurrentDataValues() {
        return new ResponseEntity<>(protocolDataService.getProtocolValueData(), HttpStatus.OK);
    }

}
