package com.diago.ship;

import com.diago.ship.dao.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class ShipmentService implements IShipment {

    @Autowired
    private RootShipmentRepo rootShipRepo;

    @Autowired
    private MergedShipmentRepo mergeShipRepo;

    @Autowired
    private StandardShipmentRepo stdShipRepo;

    /**
     * Split operation on a shipment, would create more than one shipments with specified quantities.
     * Sum of all child shipment quantities should be equal to parent shipment quantity.
     * - If weight of root shipment is 1000kg.We plan to split 3 shares. The solution is:
     * 1) rootShipment.split("1000")         // Right!
     * 2) rootShipment.split("501,499")      // Right!
     * 3) rootShipment.split("500,249,251")  // Right!
     * 4) rootShipment.split("333,333,333"); // SplitException! Total:999kg which should be 1000kg
     * 5) rootShipment.split("1001");        // SplitException! Total:1001kg which should be 1000kg
     * 6) Was split before                   // SplitException!
     *
     * @param rootShipId
     * @param weightList String ex. "500,249,251"
     * @return affectedChildShipment int
     * @throws SplitException
     */
    @Override
    @Transactional(rollbackFor = SplitException.class)
    public int split(Integer rootShipId, String weightList) throws SplitException {
        //find root shipment by id
        RootShipment rootShip = null;
        if (rootShipRepo.findById(rootShipId).isPresent()) {
            rootShip = rootShipRepo.findById(rootShipId).get();
        } else {
            log.debug("Root Shipment [{}] was not found.", rootShipId);
            throw new SplitException();
        }

        // Check comma split format of weightList
        if (!weightList.matches("\\d+(,\\d+)*")) {
            log.debug("Argument weightList [{}] format error when split shipments.", weightList);
            throw new SplitException();
        }

        //resolve weightList.
        String[] arrWeight = weightList.split(",");
        List<Integer> intWeights = new ArrayList<Integer>();
        for (int i = 0; i < arrWeight.length; i++) {
            intWeights.add(Integer.parseInt(arrWeight[i]));
        }

        //check split share  0 < n < weight/10kg (prevent case child shipment with 0 kg )
        Integer n = arrWeight.length;
        if (n == null || rootShip.getWeight() == null
                || n < 1 || n > Math.floorDiv(rootShip.getWeight(), 10)) {
            log.debug("weight error when split root shipment");
            throw new SplitException();
        }

        // Check this is persisted before, check no split before
        if (rootShip.getId() == null || (rootShip.getSpiltShipments() != null && rootShip.getSpiltShipments().size() > 0)) {
            log.debug("root is not persisted or split before when split root shipment.");
            throw new SplitException();
        }

        //split shipments weight by intWeights
        List<StandardShipment> stdShipList = new ArrayList<StandardShipment>();
        int affectRowNum = 0;
        int totalweight = 0;
        for (int i = 0; i < n; i++) {
            StandardShipment stdShip = new StandardShipment();
            stdShip.setWeight(intWeights.get(i));
            stdShip.setRootShipment(rootShip);
            stdShipList.add(stdShip);
            affectRowNum++;
            totalweight += stdShip.getWeight();
        }
        rootShip.setSpiltShipments(stdShipList);

        // weight summary/child count check after split
        if (rootShip.getWeight() != totalweight || affectRowNum != n) {
            log.debug("Weight summary/child count failed when splitting root shipment.");
            throw new SplitException();
        }

        // save
        for (StandardShipment standardShipment : stdShipList) {
            stdShipRepo.save(standardShipment);
        }

        // return Number of standard shipments being split..
        return affectRowNum;
    }

    /**
     * Merge operation on more than one shipment, would create one child shipment with summed
     * up quantity. Sum of all parent shipment quantities should be equal to child shipment quantity.
     * - If 1 root shipment split to 3 child standard shipment which id:1/2/3. Weight:111/222/333kg
     * 1) shipmentService.merge("1");     //MergeException! Merge only 1 standard shipment, at least 2.
     * 2) shipmentService.merge("1,2");   //Right! Total merge weight: 333kg
     * 3) shipmentService.merge("1,2,3"); //MergeException! id:1,2 was merged before.
     * <p>
     * Check argument standard shipments existence, Create a merge shipment.
     * Accumulate weight to merge shipment. Manage the linkage.
     *
     * @param shipIds id string with comma
     * @return int new merge shipment id.
     * @throws MergeException
     */
    @Override
    @Transactional(rollbackFor = MergeException.class)
    public int merge(String shipIds) throws MergeException {

        // Check comma split format of argument shipIds
        if (!shipIds.matches("\\d+(,\\d+)*")) {
            log.debug("Shipment ids format error when merge shipments.");
            throw new MergeException();
        }

        // count of shipments check
        String[] ids = shipIds.split(",");
        if (ids.length < 2) {
            log.debug("At lease 2 Shipments can be merged.");
            throw new MergeException();
        }

        // Check and get standard shipment from shipIds, If not exists throw MergeException.
        List<StandardShipment> mergedStdShipList = new ArrayList<StandardShipment>();
        int totalWeight = 0;
        int affectedChildren = 0;
        for (String id : ids) {
            Optional<StandardShipment> optStdShip = stdShipRepo.findById(Integer.valueOf(id));
            if (!optStdShip.isPresent()) {
                log.debug("Shipment was not found when merge shipments.");
                throw new MergeException();
            } else {
                //Check shipIds didn't merged before
                StandardShipment stdShip = optStdShip.get();
                if (stdShip.getMergeToShipment() != null) {
                    log.debug("Found a shipment[" + stdShip.getId() + "] has been merged when merging shipments.");
                    throw new MergeException();
                }
                mergedStdShipList.add(stdShip);
                totalWeight += stdShip.getWeight();
                affectedChildren++;
            }
        }

        // Create a merge shipment with total weight
        MergedShipment mergedShipment = new MergedShipment();
        mergedShipment.setWeight(totalWeight);
        mergeShipRepo.save(mergedShipment); // assign merge shipment id for future use.

        //update linkage.
        for (StandardShipment childShipment : mergedStdShipList) {
            childShipment.setMergeToShipment(mergedShipment);
        }
        mergedShipment.setMergedFromShipments(mergedStdShipList);

        Integer newMergeShipId = mergedShipment.getId();
        return (newMergeShipId == null ? 0 : newMergeShipId);
    }

    /**
     * This operation applies to trade. When trade quantity is changed, all shipment quantities should
     * be updated proportionally
     * - There is 1 root shipment 6000kg with 3 child standard shipments id:1/2/3 weight:1000/2000/3000kg
     * Merge 2&3 with total weight 5000.
     * - If there are some merged shipments on the root shipment. Find and re-accumulate them.
     * - rootShipment.changeWeight("12000") //Right! new weight:2000/4000/6000kg, mergeShipment(2&3):10000kg
     * - rootShipment.changeWeight("2000")  //Right! new weight:333/666/1001kg (The biggest part will plus
     * remainder, if they are same, max id will plus remainder.)
     * - At the end, if there is any merged shipment, trigger auto merge. ex, merge("2,3"):1667kg
     * <p>
     * Actions:
     * 1) Update weight for the child standard shipment proportionally.
     * 2) If the remainder was found, add it to the biggest part or max id part in same case.
     * 3) Find merged shipments, re-merge them.
     * 4) Update weight for this root shipment.
     *
     * @param newWeight 0 < weight < max Integer.
     * @return int affectRowNum
     * @throws SplitException
     */
    @Override
    @Transactional
    public int changeWeight(Integer rootShipId, Integer newWeight) throws SplitException, MergeException {

        //find root shipment by id
        RootShipment rootShip = null;
        if (rootShipRepo.findById(rootShipId).isPresent()) {
            rootShip = rootShipRepo.findById(rootShipId).get();
        } else {
            log.debug("Root Shipment [{}] was not found.", rootShipId);
            throw new SplitException();
        }

        //check split share  0 < weight < 1,000,000,000
        if (newWeight == null || newWeight < 1 || rootShip.getWeight() == null
                || newWeight > 1000000000) {
            log.debug("bad weight number when change root shipment.");
            throw new SplitException();
        }

        // check must split before
        if (rootShip.getId() == null || rootShip.getSpiltShipments() == null ||
                (rootShip.getSpiltShipments() != null && rootShip.getSpiltShipments().size() == 0)) {
            log.debug("This root shipment not split before when change root shipment of weight.");
            throw new SplitException();
        }

        //update weight for only 1 split standard shipment.(kg)
        if (rootShip.getSpiltShipments().size() == 1) {
            log.info("Change only 1 split shipment [" + rootShip.getSpiltShipments().get(0).getId() + "] weight from" +
                    " [" + rootShip.getSpiltShipments().get(0).getWeight() + "] to [" + newWeight + "].");
            rootShip.getSpiltShipments().get(0).setWeight(newWeight);
            return 1;
        }

        //update weight for all split standard shipments proportionally (kg)
        int line_max_weight = 0, line_max_weight_id = 0, newWeightSum = 0, affectRowNum = 0;
        for (StandardShipment spiltShipment : rootShip.getSpiltShipments()) {
            Integer newLineWeight = (int) (newWeight * ((float) spiltShipment.getWeight() / rootShip.getWeight()));
            log.info("Change split shipment [" + spiltShipment.getId() + "] weight from" +
                    " [" + spiltShipment.getWeight() + "] to [" + newLineWeight + "].");
            spiltShipment.setWeight(newLineWeight);

            // Find sum of weight and max weight and it's id(max) for updating remainder in next step.
            newWeightSum += newLineWeight;
            if (newLineWeight > line_max_weight) {
                line_max_weight = newLineWeight;
                line_max_weight_id = spiltShipment.getId();
            } else if (newLineWeight == line_max_weight) {
                if (spiltShipment.getId() > line_max_weight_id) {
                    line_max_weight_id = spiltShipment.getId();
                }
            }
            affectRowNum++;
        }

        // if remainder was found, add it to the biggest line weight, if there are 2 biggest, update the max(id)..
        int remainder = newWeight - newWeightSum;
        if (remainder > 0) {
            for (StandardShipment spiltShipment : rootShip.getSpiltShipments()) {
                if (spiltShipment.getId() == line_max_weight_id) {
                    Integer newLineWeight = spiltShipment.getWeight() + remainder;
                    log.debug("Standard shipment [" + spiltShipment.getId() + "] weight was updated from" +
                            " [" + spiltShipment.getWeight() + "] to [" + newLineWeight + "].");
                    spiltShipment.setWeight(newLineWeight);
                }
            }
        }

        // Find merged shipments, re-merge them, save merged shipments.
        Set<Integer> mergeIds = new HashSet<Integer>();
        Set<MergedShipment> mergedShipments = new HashSet<MergedShipment>();
        for (StandardShipment spiltShipment : rootShip.getSpiltShipments()) {
            if (spiltShipment.getMergeToShipment() != null && spiltShipment.getMergeToShipment().getId() != null) {
                mergeIds.add(spiltShipment.getMergeToShipment().getId());
            }
        }
        for (Integer mergeId : mergeIds) {
            Integer newMergeWeight = 0;
            MergedShipment mergedShipment = mergeShipRepo.findById(mergeId).get();
            for (StandardShipment mergedFromShipment : mergedShipment.getMergedFromShipments()) {
                newMergeWeight += mergedFromShipment.getWeight();
            }
            log.info("Merge Shipment [" + mergedShipment.getId() + "] was updated from " +
                    "[" + mergedShipment.getWeight() + "] to [" + newMergeWeight + "].");
            mergedShipment.setWeight(newMergeWeight);
            mergeShipRepo.save(mergedShipment);
        }

        // update wight of root shipment.
        log.info("Update root shipment weight from [" + rootShip.getWeight() + "] to [" + newWeight + "].");
        rootShip.setWeight(newWeight);

        // save result.
        for (StandardShipment spiltShipment : rootShip.getSpiltShipments()) {
            //stdShipRepo.save(spiltShipment);
        }
        rootShipRepo.save(rootShip);

        // return Number of standard shipments being updated weight..
        return affectRowNum;
    }

}
