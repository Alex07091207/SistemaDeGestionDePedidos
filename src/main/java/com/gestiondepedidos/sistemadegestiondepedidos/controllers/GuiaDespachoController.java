package com.gestiondepedidos.sistemadegestiondepedidos.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestiondepedidos.sistemadegestiondepedidos.entity.GuiaDespacho;
import com.gestiondepedidos.sistemadegestiondepedidos.services.GuiaDespachoService;
import com.gestiondepedidos.sistemadegestiondepedidos.services.S3Service;

@RestController
@RequestMapping("/guias")
public class GuiaDespachoController {

    @Autowired
    private GuiaDespachoService guiaDespachoService;

    @Autowired
    private S3Service s3Service;

    // 1. REQUISITO: Crear guía de despacho (Esto dispara automáticamente el flujo EFS -> S3)
    @PostMapping
    public ResponseEntity<GuiaDespacho> crearGuia(@RequestBody GuiaDespacho guiaDespacho) {
        try {
            GuiaDespacho nuevaGuia = guiaDespachoService.crearGuiaDespacho(guiaDespacho);
            return new ResponseEntity<>(nuevaGuia, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 6. REQUISITO FALTANTE: Descargar la guía desde AWS S3
    @GetMapping("/descargar")
    public ResponseEntity<byte[]> descargarGuia(@RequestParam String s3Key) {
        try {
            byte[] data = s3Service.descargarGuiaDespacho(s3Key);
            
            String nombreArchivo = s3Key.substring(s3Key.lastIndexOf("/") + 1);

            return ResponseEntity.ok()
                    .header("Content-type", "application/octet-stream")
                    .header("Content-disposition", "attachment; filename=\"" + nombreArchivo + "\"")
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    } // CORREGIDO: Se eliminó la llave extra que rompía la clase aquí

    // 2. REQUISITO: Listar todas las guías
    @GetMapping
    public ResponseEntity<List<GuiaDespacho>> obtenerTodas() {
        return new ResponseEntity<>(guiaDespachoService.obtenerTodasLasGuias(), HttpStatus.OK);
    }

    // 3. REQUISITO: Consultar guías por transportista y/o fecha (Filtros dinámicos)
    @GetMapping("/buscar")
    public ResponseEntity<List<GuiaDespacho>> buscarGuias(
            @RequestParam(required = false) Long transportistaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
        
        if (transportistaId != null && fecha != null) {
            return new ResponseEntity<>(guiaDespachoService.obtenerPorTransportistaYFecha(transportistaId, fecha), HttpStatus.OK);
        } else if (transportistaId != null) {
            return new ResponseEntity<>(guiaDespachoService.obtenerPorTransportista(transportistaId), HttpStatus.OK);
        } else if (fecha != null) {
            return new ResponseEntity<>(guiaDespachoService.obtenerPorFecha(fecha), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(guiaDespachoService.obtenerTodasLasGuias(), HttpStatus.OK);
        }
    }

    // 4. REQUISITO: Modificar o actualizar guías
    @PutMapping("/{id}")
    public ResponseEntity<GuiaDespacho> actualizarGuia(@PathVariable Long id, @RequestBody GuiaDespacho guiaDespacho) {
        try {
            GuiaDespacho guiaActualizada = guiaDespachoService.actualizarGuiaDespacho(id, guiaDespacho);
            return new ResponseEntity<>(guiaActualizada, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 5. REQUISITO: Eliminar guías específicas
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> eliminarGuia(@PathVariable Long id) {
        try {
            guiaDespachoService.eliminarGuiaDespacho(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
