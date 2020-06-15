package com.diago.ship.Controller;

import com.diago.ship.MergeException;
import com.diago.ship.ResultBox;
import com.diago.ship.ShipmentService;
import com.diago.ship.dao.MergedShipment;
import com.diago.ship.dao.MergedShipmentRepo;
import com.diago.ship.dao.RootShipment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/mergedShipment")
@Slf4j
public class MergedShipmentController {

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private MergedShipmentRepo mergeShipRepo;

    @GetMapping("")
    public List<MergedShipment> listAll() {
        log.info("Enter MergedShipment listAll()...");
        return mergeShipRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResultBox findById(@PathVariable Integer id) {
        log.info("Enter MergedShipment findById()...");
        Optional<MergedShipment> res = mergeShipRepo.findById(id);
        return ResultBox.buildBy(res);
    }

    /**
     * merge standard shipment to merged shipment with accumulate weight of standard shipments.Also linkage will be
     * updated.
     *
     * @param rootShipId root shipment id to be split.
     * @param ids        comma separated standard shipment id.
     * @return ResultBox with error code and message.
     */
    @GetMapping("merge/{rootShipId}/{ids}")
    @Transactional
    public ResultBox merge(@PathVariable Integer rootShipId, @PathVariable String ids) {
        log.info("Enter RootShipment merge()...");
        MergedShipment mergedShip = mergeShipRepo.findById(rootShipId).get();
        if (mergedShip == null) {
            return ResultBox.buildBy(-1403); // return Not Found.
        }
        Integer numSplitShip;
        RootShipment rootShipment = new RootShipment();
        try {
            numSplitShip = shipmentService.merge(ids);
        } catch (MergeException e) {
            e.printStackTrace();
            return ResultBox.buildBy(-1); //return General error.
        }
        ResultBox resBox = ResultBox.newOneByCode(0);
        resBox.setTotal(numSplitShip);
        return resBox;
    }

    @PostMapping
    public ResultBox add(@RequestBody MergedShipment mergedShipment) {
        log.info("Enter MergedShipment add() ...");
        MergedShipment res = mergeShipRepo.saveAndFlush(mergedShipment);
        return ResultBox.buildBy(res);
    }

    @PutMapping
    public ResultBox update(@RequestBody MergedShipment mergedShipment) {
        log.info("Enter MergedShipment update() ...");
        MergedShipment res = mergeShipRepo.saveAndFlush(mergedShipment);
        return ResultBox.buildBy(res);
    }

    @DeleteMapping(path = "/{id}")
    public ResultBox del(@PathVariable Integer id) {
        log.info("Enter MergedShipment del()...");
        mergeShipRepo.deleteById(id);
        return ResultBox.buildBy(1);
    }
}