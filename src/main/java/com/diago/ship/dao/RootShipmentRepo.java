package com.diago.ship.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RootShipmentRepo extends JpaRepository<RootShipment, Integer> {

}