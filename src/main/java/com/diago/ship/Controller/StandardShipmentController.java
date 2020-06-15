package com.diago.ship.Controller;

import com.diago.ship.ResultBox;
import com.diago.ship.dao.StandardShipment;
import com.diago.ship.dao.StandardShipmentRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/standardShipment")
@Slf4j
public class StandardShipmentController {

    @Autowired
    private StandardShipmentRepo repo;

    @GetMapping("")
    public List<StandardShipment> listAll() {
        log.info("Enter StandardShipment listAll()...");
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResultBox findById(@PathVariable Integer id) {
        log.info("Enter StandardShipment findById()...");
        Optional<StandardShipment> res = repo.findById(id);
        return ResultBox.buildBy(res);
    }

    @PostMapping
    public ResultBox add(@RequestBody StandardShipment standardShipment) {
        log.info("Enter StandardShipment add() ...");
        StandardShipment res = repo.saveAndFlush(standardShipment);
        return ResultBox.buildBy(res);
    }

    @PutMapping
    public ResultBox update(@RequestBody StandardShipment standardShipment) {
        log.info("Enter StandardShipment update() ...");
        StandardShipment res = repo.saveAndFlush(standardShipment);
        return ResultBox.buildBy(res);
    }

    @DeleteMapping(path = "/{id}")
    public ResultBox del(@PathVariable Integer id) {
        log.info("Enter StandardShipment del()...");
        repo.deleteById(id);
        return ResultBox.buildBy(1);
    }
}