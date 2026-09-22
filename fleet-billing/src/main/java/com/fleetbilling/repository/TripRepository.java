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
    List<Trip> findByVehicleIdAndStartTimeBetween(Long vehicleId, java.time.LocalTime start, java.time.LocalTime end);

    org.springframework.data.domain.Page<Trip> findByVehicleId(Long vehicleId, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Trip> findByStatus(com.fleetbilling.enums.TripStatus status, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Trip t " +
            "WHERE (:vehicleId IS NULL OR t.vehicle.id = :vehicleId) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:fromDate IS NULL OR t.tripDate >= :fromDate) " +
            "AND (:toDate IS NULL OR t.tripDate <= :toDate)")
    org.springframework.data.domain.Page<Trip> findByFilters(
            @org.springframework.data.repository.query.Param("vehicleId") Long vehicleId,
            @org.springframework.data.repository.query.Param("status") com.fleetbilling.enums.TripStatus status,
            @org.springframework.data.repository.query.Param("fromDate") java.time.LocalDate fromDate,
            @org.springframework.data.repository.query.Param("toDate") java.time.LocalDate toDate,
            org.springframework.data.domain.Pageable pageable);
}
