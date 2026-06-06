package com.gestiondepedidos.sistemadegestiondepedidos.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestiondepedidos.sistemadegestiondepedidos.entity.GuiaDespacho;

public interface GuiaDespachoRepository extends JpaRepository<GuiaDespacho, Long> {

    List<GuiaDespacho> findByTransportista_Id(Long transportistaId);

    @Query("SELECT g FROM GuiaDespacho g WHERE CAST(g.fechaDespacho AS date) = :fecha")
    List<GuiaDespacho> findByFecha(@Param("fecha") LocalDate fecha);

    @Query("SELECT g FROM GuiaDespacho g WHERE g.transportista.id = :transportistaId AND CAST(g.fechaDespacho AS date) = :fecha")
    List<GuiaDespacho> findByTransportistaAndFecha(@Param("transportistaId") Long transportistaId, @Param("fecha") LocalDate fecha);
}
