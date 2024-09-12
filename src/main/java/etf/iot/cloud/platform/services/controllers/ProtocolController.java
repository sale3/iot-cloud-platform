package etf.iot.cloud.platform.services.controllers;

import etf.iot.cloud.platform.services.dto.OperationResult;
import etf.iot.cloud.platform.services.dto.Protocol;
import etf.iot.cloud.platform.services.exceptions.EntityNotPresentException;
import etf.iot.cloud.platform.services.services.ProtocolService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/protocols")
public class ProtocolController {

    private final ProtocolService protocolService;

    public ProtocolController(ProtocolService protocolService) {
        this.protocolService = protocolService;
    }

    @GetMapping
    public ResponseEntity<List<Protocol>> getAll() {
        return new ResponseEntity<>(protocolService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Protocol> getById(@PathVariable("id") long id) throws EntityNotPresentException {
        return new ResponseEntity<>(protocolService.getById(id), HttpStatus.OK);
    }

    @PostMapping("/check_name")
    public ResponseEntity<OperationResult> checkIfProtocolWithTheSameNameExists(@RequestBody Protocol protocol) {
        return new ResponseEntity<>(protocolService.checkIfProtocolWithTheSameNameExists(protocol), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Protocol> createProtocol(@RequestBody Protocol protocol) {
        return new ResponseEntity<>(protocolService.createProtocol(protocol), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OperationResult> deleteProtocol(@PathVariable("id") long id) {
        return new ResponseEntity<>(protocolService.deleteProtocol(id), HttpStatus.OK);
    }

    @PostMapping("/protocol_assignment")
    public ResponseEntity<OperationResult> protocolAssignment(@RequestBody List<Protocol> protocols) {
        return new ResponseEntity<>(protocolService.protocolAssignment(protocols), HttpStatus.OK);
    }

    @PostMapping("/sync")
    public ResponseEntity<OperationResult> syncProtocols() {
        return new ResponseEntity<>(protocolService.syncProtocols(), HttpStatus.OK);
    }

}
