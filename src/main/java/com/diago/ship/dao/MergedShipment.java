package com.diago.ship.dao;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.List;

/**
 * This Entity describe Merged Shipment store structure.
 * id - pk
 * standardShipments - A List of StandardShipments group by mergedShipment
 * weight of kg.
 */
@Entity
@Table(name = "merged_shipment")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class MergedShipment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private Integer weight;

    @ToString.Exclude
    @JsonManagedReference(value = "merge")
    @OneToMany(mappedBy = "mergeToShipment", fetch = FetchType.EAGER)
    private List<StandardShipment> mergedFromShipments;

}

