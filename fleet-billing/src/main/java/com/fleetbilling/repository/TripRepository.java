package com.fleetbilling.repository;

import com.fleetbilling.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    Optional<Trip> findByExternalTripId(String externalTripId);
    boolean existsByExternalTripId(String externalTripId);
    List<Trip> findByVehicleId(Long vehicleId);
    List<Trip> findByVehicleIdAndStartTimeBetween(Long vehicleId, LocalDateTime start, LocalDateTime end);
}
