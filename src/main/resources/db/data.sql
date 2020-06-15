insert into root_shipment (id, weight) values (111111,1500);
insert into merged_shipment (id, weight) values (222222, 1001);
insert into standard_shipment (id, weight, root_ship_id) values (300001, 499, 111111);
insert into standard_shipment (id, weight, root_ship_id, merge_ship_id) values (300002, 500, 111111, 222222);
insert into standard_shipment (id, weight, root_ship_id, merge_ship_id) values (300003, 501, 111111, 222222);
