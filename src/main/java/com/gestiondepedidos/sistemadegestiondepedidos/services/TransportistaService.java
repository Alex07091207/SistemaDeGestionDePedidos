package com.gestiondepedidos.sistemadegestiondepedidos.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gestiondepedidos.sistemadegestiondepedidos.entity.Transportista;
import com.gestiondepedidos.sistemadegestiondepedidos.repository.TransportistaRepository;

@Service
public class TransportistaService {

    @Autowired
    private TransportistaRepository transportistaRepository;

    public Transportista getTransportistaById(Long id) {
        return transportistaRepository.findById(id).orElse(null);
    }
    
    public Transportista saveTransportista(Transportista transportista) {
        return transportistaRepository.save(transportista);
    }

    public List<Transportista> getAllTransportistas() {
        return transportistaRepository.findAll();
    }

}
