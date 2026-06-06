package com.gestiondepedidos.sistemadegestiondepedidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestiondepedidos.sistemadegestiondepedidos.entity.Transportista;

public interface TransportistaRepository extends JpaRepository<Transportista, Long> {

}
