2020/06/13 Initialized this document.Add DB Interface Exception parts.
2020/06/14 Added Rest Test Risk parts. 

#Design

##Requirement Analysis

#### CRUD of ROOT/STANDARD/MERGED Shipment via REST API.
  - Ex. http://localhost:8008/api/v1/rootShipment
  
####Split
 Split operation on a shipment, would create more than one shipments with specified quantities. 
 Sum of all child shipment quantities should be equal to parent shipment quantity.
   - If weight of root shipment is 1000kg.We plan to split 3 shares. The solution is:
     1) rootShipment.split("1000")         // Right!
     2) rootShipment.split("501,499")      // Right!
     3) rootShipment.split("500,299,251")  // Right!
     4) rootShipment.split("333,333,333"); // SplitException! Total:999kg which should be 1000kg
     5) rootShipment.split("1001");        // SplitException! Total:1001kg which should be 1000kg
     6) Was split before                   // SplitException!
    
####Merge
Merge operation on more than one shipment, would create one child shipment with summed 
up quantity. Sum of all parent shipment quantities should be equal to child shipment quantity.
  - If 1 root shipment split to 3 child standard shipment which id:1/2/3. Weight:111/222/333kg
    1) rootShipment.merge("1");     //MergeException! Merge only 1 standard shipment, at least 2.
    2) rootShipment.merge("1,2");   //Right! Total merge weight: 333kg
    3) rootShipment.merge("1,2,3"); //MergeException! id:1,2 was merged before.
  
####Change root quantity
This operation applies to trade. When trade quantity is changed, all shipment quantities should 
be updated proportionally
  - There is 1 root shipment 6000kg with 3 child standard shipments id:1/2/3 weight:1000/2000/3000kg
  - If there are some merged shipments on the root shipment. Find and re-accumulate them. 
    - rootShipment.changeWeight("12000") //Right! new weight:2000/4000/6000kg, mergeShipment(2&3):10000kg
    - rootShipment.changeWeight("2000")  //Right! new weight:333/666/1001kg (The biggest part will plus
       remainder, if they are same, max id will plus remainder.)
    - At the end, if there is any merged shipment, trigger auto merge("2,3"):1667kg

##Technical Framework
- JAVA 1.8
- SpringBoot
- SpringMVC 
- Hibernate / JPA
- H2 Memory database
- Junit
- IDEA
- RestController
- Maven

##DB Design

####Table root_shipment
- id int (pk) 
- weight int (kg)

####Table merged_shipment
- id int (pk)
- weight int (kg)

####Table standard_shipment
- id int (pk)
- weight int (kg)
- merge_ship_id int (fk) 
- root_ship_id int (fk)

##Interface Design
- IRootShipment
  - split("200,300")
  - static merge("3,5")
  - changeWeight(5000)

##Exception Design
- SplitException
- MergeException

## REST Design 

#### common return format:
Ref: ResultBox class
{
  "errCod":0,
  "errMsg":"OK!",
  "total":1,
  "rows":[{"id":6,"weight":6000}] //return data
 }
 
#### list all root shipment via rest
http://localhost:8008/api/v1/rootShipment
http://localhost:8008/api/v1/rootShipment/{id}
0:	
  id	6
  weight	6000
  spiltShipments	
    0:	
      id	7
      weight	1000
    1:	
      id	8
      weight	2000
    2:	
      id	9
      weight	3000

#### list all standard(split) shipment via rest
http://localhost:8008/api/v1/standardShipment/{id}
http://localhost:8008/api/v1/standardShipment
0:	
  id	7
  weight	1000
1:	
  id	8
  weight	2000
2:	
  id	9
  weight	3000

#### list all merge shipment via rest
http://localhost:8008/api/v1/mergedShipment/{id}
http://localhost:8008/api/v1/mergedShipment
0:	
  id	10
  weight	5000
  mergedFromShipments	
    0:	
      id	8
      weight	2000
    1:	
      id	9
      weight	3000

#### split root shipment via rest 
* The weight of root shipment will be separated by per split standard shipments.
http://localhost:8008/api/v1/shipment/split/6/1000,2000,3000
http://localhost:8008/api/v1/shipment/split/{rootShipId}/{weightList}
return 0 - OK!

#### merge standard shipments via rest 
* The weight of merged shipment will accumulate standard shipments merge from.
http://localhost:8008/api/v1/shipment/merge/8,9 
http://localhost:8008/api/v1/shipment/merge/{CommaSepStandardShipIds} 
return 0 - OK! New Merge Id in rows[0] section.

#### change weight of root shipment via rest 
* The new weight of root shipment will be separated by per split standard shipments.
http://localhost:8008/api/v1/rootShipment/changeWeight/155/5000
http://localhost:8008/api/v1/rootShipment/split/{rootShipId}/{newWeight}
return 0 - OK!

## Test Design
#### com.diago.ship.TU_ShipmentCrud
 - testMergedShipmentCrud
 - testRootShipmentCrud
 - testStandardShipmentCrud
 - testSplitAndChangeAndMerge

#### com.diago.ship.TU_ShipmentRest
- testGreetApi
- testShipmentSplitMergeChangeWeight

#### com.diago.ship.TU_ShipmentService
- testSplitRootShipment1
- testSplitRootShipment2
- testSplitRootShipment3
- testSplitRootShipment4
- testSplitRootShipment5
- testSplitRootShipment6
- testMergeShipment
- testChangeWeightForRoot

##Risk (TODO)
1) After root shipment splitting, its sub standard shipments may have weight 
may less than weight on root shipment. since the floor(div(weight/share)) usage.
Ex.root shipment weight is 1000kg, and split to 3 shares of standard shipments. The average 
weight of standard shipments is 333kg, total is 999kg .round total need.

2) hen change root shipment weight. the merged shipment should be recalculated.

##Also see
REQUIREMENT.md
DESIGN.md
HELP.md
README.md
