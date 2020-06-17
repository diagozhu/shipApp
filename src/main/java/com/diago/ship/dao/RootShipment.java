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

}

