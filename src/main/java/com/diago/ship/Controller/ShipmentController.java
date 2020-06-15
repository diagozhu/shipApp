package com.diago.ship.Controller;

import com.diago.ship.MergeException;
import com.diago.ship.ResultBox;
import com.diago.ship.ShipmentService;
import com.diago.ship.SplitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/shipment")
@Slf4j
public class ShipmentController {

    @Autowired
    private ShipmentService shipmentService;

    @GetMapping("/greet")
    public ResultBox splitRootShipment() throws SplitException {
        log.info("Enter Shipment greet()...");
        ResultBox ret = ResultBox.newOneByCode(0);
        List<Object> rows = new ArrayList<Object>();
        rows.add("Hello World!!!");
        ret.setRows(rows);
        return ret;
    }

    @GetMapping("/split/{rootShipId}/{weightList}")
    public ResultBox splitRootShipment(@PathVariable Integer rootShipId, @PathVariable String weightList)
            throws SplitException {
        log.info("Enter Shipment splitRootShipment({},{})...", rootShipId, weightList);
        int splitRows = shipmentService.split(rootShipId, weightList);
        return ResultBox.buildByInt(splitRows);
    }

    @GetMapping("/merge/{shipIds}")
    public ResultBox mergeShipments(@PathVariable String shipIds) throws SplitException, MergeException {
        log.info("Enter Shipment mergeShipments({})...", shipIds);
        int newMergedShipId = shipmentService.merge(shipIds);
        List<Object> rows = new ArrayList<Object>();
        rows.add(newMergedShipId);
        ResultBox rb = ResultBox.buildByInt(newMergedShipId);
        rb.setRows(rows);
        return rb;
    }

    @GetMapping("/changeWeight/{rootShipId}/{newWeight}")
    public ResultBox splitRootShipment(@PathVariable Integer rootShipId, @PathVariable Integer newWeight)
            throws MergeException, SplitException {
        log.info("Enter Shipment splitRootShipment({},{})...", rootShipId, newWeight);
        int affectedRows = shipmentService.changeWeight(rootShipId, newWeight);
        return ResultBox.buildByInt(affectedRows);
    }

}
