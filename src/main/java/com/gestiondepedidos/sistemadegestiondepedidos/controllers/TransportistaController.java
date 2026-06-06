package com.gestiondepedidos.sistemadegestiondepedidos.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestiondepedidos.sistemadegestiondepedidos.entity.Transportista;
import com.gestiondepedidos.sistemadegestiondepedidos.services.TransportistaService;

@RestController
@RequestMapping("/transportistas")
public class TransportistaController {

    @Autowired
    private TransportistaService transportistaService;

    @GetMapping("/{id}")
    public Transportista getTransportistaById(@PathVariable Long id) {
        return transportistaService.getTransportistaById(id);
    }

    @GetMapping("/all")
    public List<Transportista> getAllTransportistas() {
        return transportistaService.getAllTransportistas();
    }
    
}
