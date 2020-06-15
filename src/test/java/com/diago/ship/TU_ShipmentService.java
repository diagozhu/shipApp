package com.diago.ship;

import com.diago.ship.dao.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static junit.framework.TestCase.assertTrue;

/**
 * Split operation on a shipment, would create more than one shipments with specified quantities.
 * Sum of all child shipment quantities should be equal to parent shipment quantity.
 * - If weight of root shipment is 1000kg.We plan to split 3 shares. The solution is:
 * 1) shipmentService.split(rootShipId, "1000")         // Right!
 * 2) shipmentService.split(rootShipId, "501,499")      // Right!
 * 3) shipmentService.split(rootShipId, "500,299,251")  // Right!
 * 4) shipmentService.split(rootShipId, "333,333,333"); // SplitException! Total:999kg which should be 1000kg
 * 5) shipmentService.split(rootShipId, "1001");        // SplitException! Total:1001kg which should be 1000kg
 * 6) Was split before                   // SplitException!
 */
@RunWith(SpringRunner.class)
@SpringBootTest
class TU_ShipmentService {

    @Autowired
    private RootShipmentRepo rootShipRepo;

    @Autowired
    private MergedShipmentRepo mergeShipRepo;

    @Autowired
    private StandardShipmentRepo stdShipRepo;

    @Autowired
    private ShipmentService shipmentService;

    @Test
    @Transactional
    /**
     *  Test case shipmentService.split(rootShipId, "1000");
     */
    public void testSplitRootShipment1() throws SplitException {
        //create a root shipment
        RootShipment rootShip = new RootShipment();
        rootShip.setWeight(1000); // 1 tons
        rootShipRepo.save(rootShip);
        Assertions.assertThat(rootShip.getId()).isGreaterThan(0); // pk was generated.

        //split to 1 standard shipments
        int rows = shipmentService.split(rootShip.getId(), "1000");
        Assertions.assertThat(rows).isEqualTo(1);

        //verify 1 child shipment with 1000kg was created
        Assertions.assertThat(rootShip.getSpiltShipments().size()).isEqualTo(1);
        Assertions.assertThat(rootShip.getSpiltShipments().get(0).getWeight()).isEqualTo(1000);
    }

    @Test
    @Transactional
    /**
     *  Test case shipmentService.split(rootShipId, "501,499");
     */
    public void testSplitRootShipment2() throws SplitException {
        //create a root shipment
        RootShipment rootShip = new RootShipment();
        rootShip.setWeight(1000); // 1 tons
        rootShipRepo.save(rootShip);
        Assertions.assertThat(rootShip.getId()).isGreaterThan(0); // pk was generated.

        //sprit to 2 standard shipments
        int rows = shipmentService.split(rootShip.getId(), "501,499");
        Assertions.assertThat(rows).isEqualTo(2);

        //verify 2 child shipment with 1000kg was created
        Assertions.assertThat(rootShip.getSpiltShipments().size()).isEqualTo(2);
        Assertions.assertThat(rootShip.getSpiltShipments().get(0).getWeight()).isEqualTo(501);
        Assertions.assertThat(rootShip.getSpiltShipments().get(1).getWeight()).isEqualTo(499);
    }

    @Test
    @Transactional
    /**
     *  - shipmentService.split(rootShipId, "500,249,251")  // Right!
     */
    public void testSplitRootShipment3() throws SplitException {
        //create a root shipment
        RootShipment rootShip = new RootShipment();
        rootShip.setWeight(1000); // 1 tons
        rootShipRepo.save(rootShip);
        Assertions.assertThat(rootShip.getId()).isGreaterThan(0); // pk was generated.

        //sprit to 3 standard shipments
        int rows = shipmentService.split(rootShip.getId(), "500,249,251");
        Assertions.assertThat(rows).isEqualTo(3);

        //verify 3 child shipment with 1000kg was created
        Assertions.assertThat(rootShip.getSpiltShipments().size()).isEqualTo(3);
        Assertions.assertThat(rootShip.getSpiltShipments().get(0).getWeight()).isEqualTo(500);
        Assertions.assertThat(rootShip.getSpiltShipments().get(1).getWeight()).isEqualTo(249);
        Assertions.assertThat(rootShip.getSpiltShipments().get(2).getWeight()).isEqualTo(251);
    }

    @Test
    @Transactional
    /**
     *     - shipmentService.split(rootShipId, "333,333,333"); // SplitException! Total:999kg which should be 1000kg
     */
    public void testSplitRootShipment4() throws SplitException {
        //create a root shipment
        RootShipment rootShip = new RootShipment();
        rootShip.setWeight(1000); // 1 tons
        rootShipRepo.save(rootShip);
        Assertions.assertThat(rootShip.getId()).isGreaterThan(0); // pk was generated.

        //sprit to 3 standard shipments with less weight 999kg should throws SplitException.
        boolean thrown = false;
        try {
            int rows = shipmentService.split(rootShip.getId(), "333,333,333");
            Assertions.assertThat(rows).isEqualTo(3);
        } catch (SplitException e) {
            e.printStackTrace();
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    @Transactional
    /**
     *     - shipmentService.split(rootShipId, "1001");        // SplitException! Total:1001kg which should be 1000kg
     */
    public void testSplitRootShipment5() throws SplitException {
        //create a root shipment
        RootShipment rootShip = new RootShipment();
        rootShip.setWeight(1000); // 1 tons
        rootShipRepo.save(rootShip);
        Assertions.assertThat(rootShip.getId()).isGreaterThan(0); // pk was generated.

        //sprit to 3 standard shipments with less weight 999kg should throws SplitException.
        boolean thrown = false;
        try {
            int rows = shipmentService.split(rootShip.getId(), "1001");
            Assertions.assertThat(rows).isEqualTo(1);
        } catch (SplitException e) {
            e.printStackTrace();
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    @Transactional
    /**
     *     6) Was split before                   // SplitException!
     */
    public void testSplitRootShipment6() throws SplitException {
        //create a root shipment
        RootShipment rootShip = new RootShipment();
        rootShip.setWeight(1000); // 1 tons
        rootShipRepo.save(rootShip);
        Assertions.assertThat(rootShip.getId()).isGreaterThan(0); // pk was generated.

        //sprit to 2 standard shipments with 500,500kg
        int rows = shipmentService.split(rootShip.getId(), "500,500");
        Assertions.assertThat(rows).isEqualTo(2);

        //sprit twice will throw SplitException
        boolean thrown = false;
        try {
            rows = shipmentService.split(rootShip.getId(), "400,600");
            Assertions.assertThat(rows).isEqualTo(2);
        } catch (SplitException e) {
            e.printStackTrace();
            thrown = true;
        }
        assertTrue(thrown);
    }

    /*
     * Merge operation on more than one shipment, would create one child shipment with summed
     * up quantity. Sum of all parent shipment quantities should be equal to child shipment quantity.
     * - If 1 root shipment split to 3 child standard shipment which id:1/2/3. Weight:111/222/333kg
     * 1) rootShipment.merge("1");     //MergeException! Merge only 1 standard shipment, at least 2.
     * 2) rootShipment.merge("1,2");   //Right! Total merge weight: 333kg
     * 3) rootShipment.merge("1,2,3"); //MergeException! id:1,2 was merged before.
     */
    @Test
    @Transactional
    public void testMergeShipment() throws SplitException, MergeException {
        //create a root shipment use to invoker(), don't save it since @Transactional dno't support static method..
        //create a root shipment
        RootShipment rootShip = new RootShipment();
        rootShip.setWeight(666); //kg
        rootShipRepo.save(rootShip);
        Assertions.assertThat(rootShip.getId()).isGreaterThan(0); // pk was generated.

        //split to 3 standard shipments & verifying.
        int rows = shipmentService.split(rootShip.getId(), "111,222,333");
        Assertions.assertThat(rows).isEqualTo(3);
        //verify split: 3 child shipment with 666kg was created
        Assertions.assertThat(rootShip.getSpiltShipments().size()).isEqualTo(3);
        Assertions.assertThat(rootShip.getSpiltShipments().get(0).getWeight()).isEqualTo(111);
        Assertions.assertThat(rootShip.getSpiltShipments().get(1).getWeight()).isEqualTo(222);
        Assertions.assertThat(rootShip.getSpiltShipments().get(2).getWeight()).isEqualTo(333);

        // Merge shipment  & verifying.
        Integer id1 = rootShip.getSpiltShipments().get(0).getId();
        Integer id2 = rootShip.getSpiltShipments().get(1).getId();
        Integer id3 = rootShip.getSpiltShipments().get(2).getId();
        // Test case 1) rootShipment.merge("1");     //MergeException! Merge only 1 standard shipment, at least 2.
        // verify merge shipment: will throw MergeException, at lease 2. Any rootShip can invoke merge().
        boolean thrown = false;
        try {
            int affectedRows = shipmentService.merge("" + id1);
        } catch (MergeException e) {
            e.printStackTrace();
            thrown = true;
        }
        assertTrue(thrown);

        // Test case 2) rootShipment.merge("1,2");   //Right! Total merge weight: 333kg
        // merge 2 shipment right case
        int newMergeShipId = shipmentService.merge(id1 + "," + id2);
        Assertions.assertThat(newMergeShipId).isGreaterThan(0); // return new merge shipment id.
        //verify merge weight
        MergedShipment mergeShip = mergeShipRepo.findById(newMergeShipId).get();
        Assertions.assertThat(mergeShip.getWeight()).isEqualTo(333);
        //check weight sum
        int totalWeight = 0;
        for (StandardShipment mergedFromShipment : mergeShip.getMergedFromShipments()) {
            totalWeight += mergedFromShipment.getWeight();
        }
        Assertions.assertThat(totalWeight).isEqualTo(333);

        //Test case 3) rootShipment.merge("1,2,3"); //MergeException! id:1,2 was merged before.
        boolean thrown2 = false;
        try {
            int newMergeShipId2 = shipmentService.merge(id1 + "," + id2 + "," + id3);
        } catch (MergeException e) {
            e.printStackTrace();
            thrown2 = true;
        }
        assertTrue(thrown2);
    }

    /**
     * This operation applies to trade. When trade quantity is changed, all shipment quantities should
     * be updated proportionally
     * - 1) There is 1 root shipment 6000kg with 3 child standard shipments id:1/2/3 weight:1000/2000/3000kg
     * Merge 2&3 with total weight 5000.
     * - 2) If there are some merged shipments on the root shipment. Find and re-accumulate them.
     * 1.rootShipment.changeWeight("12000") //Right! new weight:2000/4000/6000kg, mergeShipment(2&3):10000kg
     * 2.rootShipment.changeWeight("2000")  //Right! new weight:333/666/1001kg (The biggest part will plus
     * remainder, if they are same, max id will plus remainder.). merge("2,3"):1667kg
     */
    @Test
    @Transactional
    public void testChangeWeightForRoot() throws SplitException, MergeException {

        // 1) There is 1 root shipment 6000kg with 3 child standard shipments id:1/2/3 weight:1000/2000/3000kg
        // create root shipment
        RootShipment rootShip = new RootShipment();
        rootShip.setWeight(6000); //kg
        rootShipRepo.save(rootShip);
        Assertions.assertThat(rootShip.getId()).isGreaterThan(0); // pk was generated.

        // split root shipment & verifying.
        int rows = shipmentService.split(rootShip.getId(), "1000,2000,3000");
        Assertions.assertThat(rows).isEqualTo(3);
        // verify split: 3 child shipment with 6000kg was created
        Assertions.assertThat(rootShip.getSpiltShipments().size()).isEqualTo(3);
        Assertions.assertThat(rootShip.getSpiltShipments().get(0).getWeight()).isEqualTo(1000);
        Assertions.assertThat(rootShip.getSpiltShipments().get(1).getWeight()).isEqualTo(2000);
        Assertions.assertThat(rootShip.getSpiltShipments().get(2).getWeight()).isEqualTo(3000);

        // merge shipment 2 & 3 & verifying.
        Integer id2 = rootShip.getSpiltShipments().get(1).getId();
        Integer id3 = rootShip.getSpiltShipments().get(2).getId();
        int newMergeShipId = shipmentService.merge(id2 + "," + id3);
        Assertions.assertThat(newMergeShipId).isGreaterThan(0); // return new merge shipment id.
        // verify merge shipment
        MergedShipment mergeShip = mergeShipRepo.findById(newMergeShipId).get();
        Assertions.assertThat(mergeShip.getWeight()).isEqualTo(5000);
        //check weight sum = 5000 kg
        int totalWeight = 0;
        for (StandardShipment mergedFromShipment : mergeShip.getMergedFromShipments()) {
            totalWeight += mergedFromShipment.getWeight();
        }
        Assertions.assertThat(totalWeight).isEqualTo(5000);

        // Change root shipment weight. & verifying.
        // 2) If there are some merged shipments on the root shipment. Find and re-accumulate them.
        //    1.rootShipment.changeWeight("12000") //Right! new weight:2000/4000/6000kg, mergeShipment(2&3):10000kg
        int affectedRows = shipmentService.changeWeight(rootShip.getId(), 12000);
        Assertions.assertThat(affectedRows).isEqualTo(3);
        // verify 3 child shipments with 12000kg was created
        Assertions.assertThat(rootShip.getSpiltShipments().size()).isEqualTo(3);
        Assertions.assertThat(rootShip.getSpiltShipments().get(0).getWeight()).isEqualTo(2000);
        Assertions.assertThat(rootShip.getSpiltShipments().get(1).getWeight()).isEqualTo(4000);
        Assertions.assertThat(rootShip.getSpiltShipments().get(2).getWeight()).isEqualTo(6000);
        //verify merged weight
        Assertions.assertThat(mergeShip.getWeight()).isEqualTo(10000);

        // Change root shipment weight. & verifying.
        // 2) If there are some merged shipments on the root shipment. Find and re-accumulate them.
        //    2.rootShipment.changeWeight("2000")  //Right! new weight:333/666/1001kg (The biggest part will plus
        //      remainder, if they are same, max id will plus remainder.). merge("2,3"):1667kg
        int affectedRows2 = shipmentService.changeWeight(rootShip.getId(), 2000);
        Assertions.assertThat(affectedRows2).isEqualTo(3);
        // verify 3 child shipments with 2000kg was created
        Assertions.assertThat(rootShip.getSpiltShipments().size()).isEqualTo(3);
        Assertions.assertThat(rootShip.getSpiltShipments().get(0).getWeight()).isEqualTo(333);
        Assertions.assertThat(rootShip.getSpiltShipments().get(1).getWeight()).isEqualTo(666);
        Assertions.assertThat(rootShip.getSpiltShipments().get(2).getWeight()).isEqualTo(1001);
        //verify merged weight
        Assertions.assertThat(mergeShip.getWeight()).isEqualTo(1667);

    }
}