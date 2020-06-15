package com.diago.ship.dao;

import com.diago.ship.SplitException;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This Entity describe Root Shipment store structure.
 * id - pk
 * standardShipments - A List of StandardShipments split by Root.
 * weight of kg.
 */
@Entity
@Table(name = "root_shipment")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class RootShipment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private Integer weight;

    @ToString.Exclude
    @JsonManagedReference(value = "root")
    @OneToMany(mappedBy = "rootShipment", fetch = FetchType.EAGER)
    private List<StandardShipment> spiltShipments;


    /**
     * check n
     * weight will be split by per.
     *
     * @param n 0 < n < weight/10(at least 1kg)
     * @return int affectRowNum
     * @throws SplitException
     */
    @Transactional
    public int splitNShares(RootShipmentRepo rootShipRepo, StandardShipmentRepo stdShipRepo, Integer n) throws SplitException {

        //check split share  0 > n < weight/10kg (prevent case all sub shipment 0 kg )
        if (n == null || this.weight == null
                || n < 1 || n > Math.floorDiv(this.getWeight(), 10)) {
            log.debug("weight error when split root shipment");
            throw new SplitException();
        }

        // Check this is persisted before, check no split before
        if (this.id == null || (this.spiltShipments != null && this.spiltShipments.size() > 0)) {
            log.debug("root is not persisted or split before when split root shipment.");
            throw new SplitException();
        }

        //split shipments weight by per (kg)
        int perWeight = Math.floorDiv(this.getWeight(), n);
        List<StandardShipment> stdShipList = new ArrayList<StandardShipment>();
        int affectRowNum = 0;
        for (int i = 0; i < n; i++) {
            StandardShipment stdShip = new StandardShipment();
            stdShip.setWeight(perWeight);
            stdShip.setRootShipment(this);
            stdShipList.add(stdShip);
            affectRowNum++;
        }
        this.setSpiltShipments(stdShipList);

        //save split result.
        //rootShipRepo.saveAndFlush(this);
        for (StandardShipment standardShipment : stdShipList) {
            stdShipRepo.saveAndFlush(standardShipment);
        }

        // return Number of standard shipments being split..
        return affectRowNum;
    }
}

