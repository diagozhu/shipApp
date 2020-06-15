package com.diago.ship.Controller;

import com.diago.ship.MergeException;
import com.diago.ship.ResultBox;
import com.diago.ship.ShipmentService;
import com.diago.ship.SplitException;
import com.diago.ship.dao.RootShipment;
import com.diago.ship.dao.RootShipmentRepo;
import com.diago.ship.dao.StandardShipmentRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/rootShipment")
@Slf4j
public class RootShipmentController {

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private RootShipmentRepo rootShipRepo;

    @Autowired
    private StandardShipmentRepo stdShipRepo;

    @GetMapping("")
    public List<RootShipment> listAll() {
        log.info("Enter RootShipment listAll()...");
        return rootShipRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResultBox findById(@PathVariable Integer id) {
        log.info("Enter RootShipment findById()...");
        RootShipment res = rootShipRepo.findById(id).get();
        return ResultBox.buildBy(res);
    }

    /**
     * split root shipment to standard shipments, the weight will be split by per.
     *
     * @param rootShipId root shipment id to be split.
     * @param share      number to split.
     * @return ResultBox with error code and message.
     */
    @GetMapping("split/{rootShipId}/{share}")
    @Transactional
    public ResultBox split(@PathVariable Integer rootShipId, @PathVariable Integer share) {
        log.info("Enter RootShipment split()...");
        RootShipment rootShip = rootShipRepo.findById(rootShipId).get();
        if (rootShip == null) {
            return ResultBox.buildBy(-1403); // return Not Found.
        }
        Integer numSplitShip;
        try {
            numSplitShip = rootShip.splitNShares(rootShipRepo, stdShipRepo, share);
        } catch (SplitException e) {
            e.printStackTrace();
            return ResultBox.buildBy(-1); //return General error.
        }
        ResultBox ret = ResultBox.newOneByCode(0);
        ret.setTotal(numSplitShip);
        return ret;
    }

    /**
     * change weight of root shipment,the split standard shipments of weight also should be change by per.
     *
     * @param rootShipId root shipment id to be split.
     * @param weight     new weight with uom of kg
     * @return ResultBox with error code and message.
     */
    @GetMapping("changeWeight/{rootShipId}/{weight}")
    @Transactional
    public ResultBox changeWeight(@PathVariable Integer rootShipId, @PathVariable Integer weight) throws MergeException {
        log.info("Enter RootShipment changeWeight()...");
        RootShipment rootShip = rootShipRepo.findById(rootShipId).get();
        if (rootShip == null) {
            return ResultBox.buildBy(-1403); // return Not Found.
        }
        Integer numSplitShip;
        try {
            numSplitShip = shipmentService.changeWeight(rootShipId, weight);
        } catch (SplitException e) {
            e.printStackTrace();
            return ResultBox.buildBy(-1); //return General error.
        }
        ResultBox ret = ResultBox.newOneByCode(0);
        ret.setTotal(numSplitShip);
        return ret;
    }

    @PostMapping
    public ResultBox add(@RequestBody RootShipment rootShipment) {
        log.info("Enter RootShipment add() ...");
        RootShipment res = rootShipRepo.saveAndFlush(rootShipment);
        return ResultBox.buildBy(res);
    }

    @PutMapping
    public ResultBox update(@RequestBody RootShipment rootShipment) {
        log.info("Enter RootShipment update() ...");
        RootShipment res = rootShipRepo.saveAndFlush(rootShipment);
        return ResultBox.buildBy(res);
    }

    @DeleteMapping(path = "/{id}")
    public ResultBox del(@PathVariable Integer id) {
        log.info("Enter RootShipment del()...");
        rootShipRepo.deleteById(id);
        return ResultBox.buildBy(1);
    }
}