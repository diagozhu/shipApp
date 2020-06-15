package com.diago.ship.dao;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * This Entity describe a standard shipment store structure.
 * id is pk
 * root_ship_id (rootShipment): represent this row is a split from a root shipment.
 * merge_ship_id (mergeToShipment): represent this row is a part of merged shipment.
 */
@Entity
@Table(name = "standard_shipment")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardShipment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private Integer Weight;

    @JsonBackReference(value = "root")
    @ManyToOne
    @JoinColumn(name = "root_ship_id")
    private RootShipment rootShipment;

    @JsonBackReference(value = "merge")
    @ManyToOne
    @JoinColumn(name = "merge_ship_id")
    private MergedShipment mergeToShipment;

}
