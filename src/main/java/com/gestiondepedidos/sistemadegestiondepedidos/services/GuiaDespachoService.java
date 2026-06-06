package com.gestiondepedidos.sistemadegestiondepedidos.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gestiondepedidos.sistemadegestiondepedidos.entity.GuiaDespacho;
import com.gestiondepedidos.sistemadegestiondepedidos.entity.Transportista;
import com.gestiondepedidos.sistemadegestiondepedidos.repository.GuiaDespachoRepository;
import com.gestiondepedidos.sistemadegestiondepedidos.repository.TransportistaRepository;

@Service
public class GuiaDespachoService {

    @Autowired
    private TransportistaRepository transportistaRepository;

    @Autowired
    private GuiaDespachoRepository guiaDespachoRepository;

    @Autowired
    private S3Service s3Service;

    @Value("${efs.mount-path}")
    private String efsMountPath;

    public GuiaDespacho crearGuiaDespacho(GuiaDespacho guia) {
        if (guia.getTransportista() == null || guia.getTransportista().getId() == null) {
            throw new IllegalArgumentException("El transportista es obligatorio");
        }
        Transportista transportista = transportistaRepository.findById(guia.getTransportista().getId())
                .orElseThrow(() -> new RuntimeException("Transportista no encontrado"));

        if (guia.getDireccionDestino() == null || guia.getDireccionDestino().isBlank()) {
            throw new IllegalArgumentException("La dirección de destino es obligatoria");
        }

        guia.setTransportista(transportista);
        if (guia.getFechaDespacho() == null) {
            guia.setFechaDespacho(LocalDateTime.now());
        }

        // 1. Guardar registro inicial para obtener la ID generada de la guía
        GuiaDespacho guiaGuardada = guiaDespachoRepository.save(guia);

        // 2. PASO EFS: Crear el archivo de texto simulando el PDF en la ruta compartida
        String nombreArchivo = "guia" + guiaGuardada.getId() + ".pdf";
        File archivoTemporal = manejarArchivoTemporalEFS(guiaGuardada, nombreArchivo);

        // 3. PASO S3: Subir a la nube y obtener la key estructurada (/fecha/transportista/nombre)
        String keyGenerada = s3Service.subirGuiaDespacho(archivoTemporal, guia.getTransportista().getId(), nombreArchivo);

        // 4. CORREGIDO: Guardar la key del archivo en la base de datos
        guiaGuardada.setS3Key(keyGenerada);
        return guiaDespachoRepository.save(guiaGuardada);
    }

    private File manejarArchivoTemporalEFS(GuiaDespacho guia, String nombreArchivo) {
        try {
            Path pathEfs = Paths.get(efsMountPath);
            if (!Files.exists(pathEfs)) {
                Files.createDirectories(pathEfs);
            }

            File archivo = new File(efsMountPath + File.separator + nombreArchivo);
            try (FileWriter writer = new FileWriter(archivo)) {
                writer.write("Guia de Despacho ID: " + guia.getId() + "\n");
                writer.write("Transportista: " + guia.getTransportista().getNombre() + " " + guia.getTransportista().getApellidoPaterno() + "\n");
                writer.write("Destino: " + guia.getDireccionDestino() + "\n");
                writer.write("Fecha: " + guia.getFechaDespacho().toString() + "\n");
            }
            return archivo;
        } catch (IOException e) {
            throw new RuntimeException("Error al gestionar el almacenamiento temporal en EFS", e);
        }
    }

    public List<GuiaDespacho> obtenerTodasLasGuias() {
        return guiaDespachoRepository.findAll();
    }

    public List<GuiaDespacho> obtenerPorTransportista(Long transportistaId) {
        return guiaDespachoRepository.findByTransportista_Id(transportistaId);
    }

    public List<GuiaDespacho> obtenerPorFecha(LocalDateTime fecha) {
        LocalDate date = fecha.toLocalDate();
        return guiaDespachoRepository.findByFecha(date);
    }

    public List<GuiaDespacho> obtenerPorTransportistaYFecha(Long transportistaId, LocalDateTime fecha) {
        LocalDate date = fecha.toLocalDate();
        return guiaDespachoRepository.findByTransportistaAndFecha(transportistaId, date);
    }

    public void eliminarGuiaDespacho(Long id) {
        guiaDespachoRepository.deleteById(id);
    }
    
    public GuiaDespacho actualizarGuiaDespacho(Long id, GuiaDespacho guiaDespacho) {
        GuiaDespacho guiaExistente = guiaDespachoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guia de despacho no encontrada"));
        
        if (guiaDespacho.getTransportista() != null && guiaDespacho.getTransportista().getId() != null) {
            Transportista transportista = transportistaRepository.findById(guiaDespacho.getTransportista().getId())
                    .orElseThrow(() -> new RuntimeException("Transportista no encontrado"));
            guiaExistente.setTransportista(transportista);
        }
        
        if (guiaDespacho.getDireccionDestino() != null && !guiaDespacho.getDireccionDestino().isBlank()) {
            guiaExistente.setDireccionDestino(guiaDespacho.getDireccionDestino());
        }
        
        if (guiaDespacho.getFechaDespacho() != null) {
            guiaExistente.setFechaDespacho(guiaDespacho.getFechaDespacho());
        }
        
        return guiaDespachoRepository.save(guiaExistente);
    }
}