package com.gestiondepedidos.sistemadegestiondepedidos.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gestiondepedidos.sistemadegestiondepedidos.entity.GuiaDespacho;
import com.gestiondepedidos.sistemadegestiondepedidos.repository.GuiaDespachoRepository;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer; // Importación crucial
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Autowired
    GuiaDespachoRepository guiaDespachoRepository;

    // 1. SUBIR ARCHIVO CON ESTRUCTURA DE CARPETAS (Evaluado en pauta)
    public String subirGuiaDespacho(File archivoFisico, Long transportistaId, String nombreArchivo) {
        String fechaCarpeta = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String s3Key = fechaCarpeta + "/transportista" + transportistaId + "/" + nombreArchivo;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        s3Client.putObject(request, RequestBody.fromFile(archivoFisico));
        return s3Key; 
    }

    // 2. DESCARGAR ARCHIVO (Evaluado en pauta)
    public byte[] descargarGuiaDespacho(String s3Key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        // CORREGIDO: Uso estándar de ResponseTransformer en el SDK v2
        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObject(request, ResponseTransformer.toBytes());
        return objectBytes.asByteArray();
    }

  // Eliminar archivo en S3
public void eliminarGuiaDespacho(String s3Key) {
    DeleteObjectRequest request = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Key)
            .build();
    s3Client.deleteObject(request);
}

// Actualizar archivo en S3
public String actualizarGuiaEnS3(String s3KeyAntiguo, File archivoNuevo, Long transportistaId, String nombreArchivo) {
    // Eliminar archivo antiguo si existe
    if (s3KeyAntiguo != null && !s3KeyAntiguo.isEmpty()) {
        eliminarGuiaDespacho(s3KeyAntiguo);  
    }
    
    // Subir archivo nuevo
    return subirGuiaDespacho(archivoNuevo, transportistaId, nombreArchivo);
}
    
    // 2. Subir archivo nuevo con estructura actualizada
}

