package etf.iot.cloud.platform.services.controllers;

import etf.iot.cloud.platform.services.dto.OperationResult;
import etf.iot.cloud.platform.services.dto.Protocol;
import etf.iot.cloud.platform.services.dto.ProtocolData;
import etf.iot.cloud.platform.services.dto.ProtocolDataSubmission;
import etf.iot.cloud.platform.services.exceptions.EntityNotPresentException;
import etf.iot.cloud.platform.services.services.ProtocolDataService;
import etf.iot.cloud.platform.services.services.ProtocolService;
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

}
