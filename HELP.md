# Author: Diago Zhu diago@yeah.net 13818118656

# Getting Started
1) Please read README.md as a Manual or Design.

2) You can review and run Test Cases with out ShipApp running:
- TU_ShipmentCrud
- TU_ShipmentRest
- TU_ShipmentService

3) Start ShipApplication(IDEA or jar) via 8008 port. Then you can see the data structure by REST API:
#### http://localhost:8008/api/v1/rootShipment
#### http://localhost:8008/api/v1/standardShipment
#### http://localhost:8008/api/v1/mergedShipment

4) if you wang to create/split/merge/changeWeight shipment, please use following REST API.
#### create root shipment
##### Request:
POST http://localhost:8008/api/v1/rootShipment {"weight":6000}
With header property: Content-Type=application/json;charset=UTF-8

##### Response:
{
     "errCod": 0,
     "errMsg": "Create OK.",
     "total": 1,
     "rows": [{
       "id": 2,
       "weight": 6000,
       "spiltShipments": null
     }]
}
#### split root shipment 
// The weight of root shipment will be separated by per split standard shipments.
GET http://localhost:8008/api/v1/shipment/split/2/1000,2000,3000
GET http://localhost:8008/api/v1/shipment/split/{rootShipId}/{weightList}
return 0 - OK!
//check split shipment data structure.
GET http://localhost:8008/api/v1/rootShipment //ex. 7,8,9

#### merge standard(split) shipments 
// The weight of merged shipment will accumulate standard shipments merge from.
GET http://localhost:8008/api/v1/shipment/merge/8,9 //new standard_shipment_id
GET http://localhost:8008/api/v1/shipment/merge/{CommaSepStandardShipIds} 
return 0 - OK! New Merge Id in rows[0] section.
// Verify merge shipment.
http://localhost:8008/api/v1/mergedShipment

#### change weight of root shipment 
* The new weight of root shipment will be separated by per split standard shipments.
http://localhost:8008/api/v1/rootShipment/changeWeight/2/12000
http://localhost:8008/api/v1/rootShipment/changeWeight/2/2000
http://localhost:8008/api/v1/rootShipment/split/{rootShipId}/{newWeight}
return 0 - OK!
// Verify root/standard/merge shipment.
GET http://localhost:8008/api/v1/rootShipment
GET http://localhost:8008/api/v1/standardShipment
GET http://localhost:8008/api/v1/mergedShipment

#Also see
REQUIREMENT.md
HELP.md
README.md
