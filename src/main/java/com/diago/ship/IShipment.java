package com.diago.ship;

/**
 * Shipment service interface. 3 funcitons: split / merge / changeWeight
 */
public interface IShipment {

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
     * @param weightList String ex. "500,249,251"
     * @return affectedChildShipment int
     * @throws SplitException
     */
    int split(Integer rootShipId, String weightList) throws SplitException;

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
     * @param shipIds standard shipments id string with comma
     * @return a number of merged from shipments
     * @throws MergeException
     */
    int merge(String shipIds) throws MergeException;

    /**
     * This operation applies to trade. When trade quantity is changed, all shipment quantities should
     * be updated proportionally
     * - There is 1 root shipment 6000kg with 3 child standard shipments id:1/2/3 weight:1000/2000/3000kg
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
    int changeWeight(Integer rootShipId, Integer newWeight) throws SplitException, MergeException;


}
