package com.diago.ship;

import com.diago.ship.dao.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

/**
 * TEST CASE for CRUD:
 * No. | action  | verify
 * 2   |COUNT    | save init Count
 * 2   |CREATE 1 | findById
 * 4   |UPDATE 1 | FindById, check changed column
 * 5   |DEL 1    | equals init Count
 */

@RunWith(SpringRunner.class)
@SpringBootTest
class TU_ShipmentCrud {

    @Autowired
    private RootShipmentRepo rootShipRepo;

    @Autowired
    private MergedShipmentRepo mergeShipRepo;

    @Autowired
    private StandardShipmentRepo stdShipRepo;

    @Autowired
    private ShipmentService shipmentService;

    /**
     * MergedShipment CRUD test.
     */
    @Test
    public void testMergedShipmentCrud() {
        long initCount;
        Integer lastInsertId;

        // Create a record
        initCount = mergeShipRepo.count();
        MergedShipment mship = new MergedShipment();
        mship.setWeight(10000);
        mergeShipRepo.save(mship);
        lastInsertId = mship.getId();
        System.out.println(lastInsertId);
        Assertions.assertThat(mergeShipRepo.count()).isEqualTo(initCount + 1);

        // Change a record
        MergedShipment mship2 = mergeShipRepo.findById(lastInsertId).get();
        Assertions.assertThat(mship2.getWeight()).isEqualTo(10000);
        mship2.setWeight(20000);
        mergeShipRepo.save(mship2);

        // Delete a record
        MergedShipment mship3 = mergeShipRepo.findById(lastInsertId).get();
        Assertions.assertThat(mship3.getWeight()).isEqualTo(20000);
        mergeShipRepo.deleteById(lastInsertId);
        mergeShipRepo.flush();
        Assertions.assertThat(mergeShipRepo.count()).isEqualTo(initCount);

    }

    /**
     * RootShipment CRUD test.
     */
    @Test
    public void testRootShipmentCrud() {
        long initCount;
        Integer lastInsertId;

        // Create a record
        initCount = rootShipRepo.count();
        RootShipment rship = new RootShipment();
        rship.setWeight(10000);
        rootShipRepo.save(rship);
        lastInsertId = rship.getId();
        System.out.println(lastInsertId);
        Assertions.assertThat(rootShipRepo.count()).isEqualTo(initCount + 1);

        // Change a record
        RootShipment rship2 = rootShipRepo.findById(lastInsertId).get();
        Assertions.assertThat(rship2.getWeight()).isEqualTo(10000);
        rship2.setWeight(20000);
        rootShipRepo.save(rship2);

        // Delete a record
        RootShipment rship3 = rootShipRepo.findById(lastInsertId).get();
        Assertions.assertThat(rship3.getWeight()).isEqualTo(20000);
        rootShipRepo.deleteById(lastInsertId);
        rootShipRepo.flush();
        Assertions.assertThat(rootShipRepo.count()).isEqualTo(initCount);

    }

    /**
     * Standard Shipment CRUD test.
     */
    @Test
    public void testStandardShipmentCrud() {
        long initCount;
        Integer lastInsertId;

        // Create a record
        initCount = stdShipRepo.count();
        StandardShipment sship = new StandardShipment();
        sship.setWeight(10000);
        stdShipRepo.save(sship);
        lastInsertId = sship.getId();
        System.out.println(lastInsertId);
        Assertions.assertThat(stdShipRepo.count()).isEqualTo(initCount + 1);

        // Change a record
        StandardShipment sship2 = stdShipRepo.findById(lastInsertId).get();
        Assertions.assertThat(sship2.getWeight()).isEqualTo(10000);
        sship2.setWeight(20000);
        stdShipRepo.save(sship2);

        // Delete a record
        StandardShipment sship3 = stdShipRepo.findById(lastInsertId).get();
        Assertions.assertThat(sship3.getWeight()).isEqualTo(20000);
        stdShipRepo.deleteById(lastInsertId);
        stdShipRepo.flush();
        Assertions.assertThat(stdShipRepo.count()).isEqualTo(initCount);

    }

    /**
     * 1 Split root shipment to Standard Shipments test.
     * 2 Change weight of root shipment and the weight of sub standard shipments will be updated by per again.
     * 3 Merge standard shipments to a merged shipment with accumulating weight. Also the linkage was bound..
     */
    //@Test
    @Transactional
    public void testSplitAndChangeAndMerge() throws SplitException, MergeException {

        //create a root shipment
        RootShipment rootShip = new RootShipment();
        rootShip.setWeight(100000); // 100 tons
        rootShipRepo.save(rootShip);
        Integer lastInsertId = rootShip.getId();

        //sprit to 4 standard shipments
        RootShipment rootShip2 = rootShipRepo.findById(lastInsertId).get();
        int rows = rootShip2.splitNShares(rootShipRepo, stdShipRepo, 4);
        Assertions.assertThat(rows).isEqualTo(4);


        // Check 4 split shipment was split from root shipment.
        RootShipment rootShip3 = rootShipRepo.findById(lastInsertId).get();
        Assertions.assertThat(rootShip3.getSpiltShipments().size()).isEqualTo(4);
        for (StandardShipment spiltShipment : rootShip3.getSpiltShipments()) {
            Assertions.assertThat(spiltShipment.getWeight()).isEqualTo(25000);// 4 * 25 tons
        }

        // change weight of root shipment to 88,880kg
        shipmentService.changeWeight(rootShip3.getId(), 88880);

        // the sub standard of weight become 10 tons = 22220 kg
        RootShipment rootShip4 = rootShipRepo.findById(lastInsertId).get();
        Assertions.assertThat(rootShip4.getSpiltShipments().size()).isEqualTo(4);
        List<Integer> splitShipIds = new ArrayList<Integer>();
        for (StandardShipment spiltShipment : rootShip4.getSpiltShipments()) {
            Assertions.assertThat(spiltShipment.getWeight()).isEqualTo(22220);// 4 * 22220 kg
            splitShipIds.add(spiltShipment.getId()); // get ids from split shipments for merge below.
        }

        // change weight of root shipment should throws SplitException.
        boolean thrown = false;
        try {
            shipmentService.changeWeight(rootShip4.getId(), 0);
        } catch (SplitException e) {
            thrown = true;
        }
        assertTrue(thrown);

        //test of merge shipment
        MergedShipment mergeShip = new MergedShipment();
        String mergerFromIds = "" + splitShipIds.get(0) + "," + splitShipIds.get(2);
        shipmentService.merge(mergerFromIds);
        Integer lastInsertMergeShipId = mergeShip.getId();

        //verify merge
        MergedShipment mergeShip2 = mergeShipRepo.findById(lastInsertMergeShipId).get();
        Assertions.assertThat(mergeShip2.getMergedFromShipments().size()).isEqualTo(2);
        Assertions.assertThat(mergeShip2.getWeight()).isEqualTo(44440);
        int mergeFromCount = 0;
        for (StandardShipment mergeFromShipment : mergeShip2.getMergedFromShipments()) {
            Assertions.assertThat(mergeFromShipment.getWeight()).isEqualTo(22220);// 4 * 22220 kg.
            mergeFromCount++;
        }
        Assertions.assertThat(mergeFromCount).isEqualTo(2);

    }
}
