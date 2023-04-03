package com.example.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.entities.Cliente;
import com.example.model.FileUploadResponse;
import com.example.services.ClienteService;
import com.example.utilities.FileDownloadUtil;
import com.example.utilities.FileuploadUtil;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private FileuploadUtil fileuploadUtil;

    // La siguiente dependencia se inyectará por constructor (tenemos que meter
    // @RequiredArgsContructor) aunque tambien se podría con Autowired,
    private final FileDownloadUtil fileDownloadUtil;

    @GetMapping
    public ResponseEntity<List<Cliente>> findAll(@RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {

        ResponseEntity<List<Cliente>> responseEntity = null;
        List<Cliente> clientes = new ArrayList<>();
        Sort sortByNombre = Sort.by("nombre");

        if (page != null && size != null) {

            try {
                Pageable pageable = PageRequest.of(page, size, sortByNombre);
                Page<Cliente> clientesPaginados = clienteService.findAll(pageable);
                clientes = clientesPaginados.getContent();
                responseEntity = new ResponseEntity<List<Cliente>>(clientes, HttpStatus.OK);

            } catch (Exception e) {
                responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

        } else {
            try {
                clientes = clienteService.findAll(sortByNombre);
                responseEntity = new ResponseEntity<List<Cliente>>(clientes, HttpStatus.OK);
            } catch (Exception e) {
                responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        }
        return responseEntity;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable(name = "id") Integer id) {

        ResponseEntity<Map<String, Object>> responseEntity = null;

        Map<String, Object> responseAsMap = new HashMap<>();

        try {
            Cliente cliente = clienteService.findById(id);

            if (cliente != null) {
                String successMessage = "Se ha encontrado el cliente con id: " + id + " correctamente";
                responseAsMap.put("mensaje", successMessage);
                responseAsMap.put("cliente", cliente);
                // responseAsMap.put("mascotas", cliente.getMascotas());
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.OK);

            } else {
                String errorMessage = "No se ha encontrado el cliente con id: " + id;
                responseAsMap.put("error", errorMessage);
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            String errorGrave = "Error grave";
            responseAsMap.put("error", errorGrave);
            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }

    /**
     * El método siguiente persiste un producto en la base de datos
     * @throws IOException
     */
        // recibe en el cuerpo un JSON que es el producto q queremos dar de alta. EL producto viene dentro de la peticion cuando es post
        // si es get viene el producto en la cabecera. El metodo insert recibe en el cuerpo de la peticion @Requestbody el producto
        // Se valida el producto que ha llegado con @Valid (tiene q cumplir con los requisitos puestos en la entidad)
        //BindingResult: 


         // GUARDAR (Persistir), un producto, con su presentacion en la base de datos
    // Para probarlo con POSTMAN: Body -> form-data -> producto -> CONTENT TYPE ->
    // application/json
    // no se puede dejar el content type en Auto, porque de lo contrario asume
    // application/octet-stream
    // y genera una exception MediaTypeNotSupported
    @PostMapping( consumes = "multipart/form-data")
    @Transactional
    public ResponseEntity<Map<String,Object>> insert(
        @Valid
        @RequestPart(name = "cliente") Cliente cliente,
         BindingResult result,
         @RequestPart(name = "file") MultipartFile file) throws IOException{
           
            
        Map<String, Object> responseAsMap = new HashMap<>();

        ResponseEntity<Map<String, Object>> responseEntity = null;

        // Primero comprobar si hay errores en el cliente recibido:
        // Getallerrors me da los errores, pero no un string si no un objeto
            if(result.hasErrors()){
                List<String> errorMessages = new ArrayList<>();

                for (ObjectError error: result.getAllErrors()) {
                    errorMessages.add(error.getDefaultMessage());
                }
                
                responseAsMap.put("errores", errorMessages);

                responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap,HttpStatus.BAD_REQUEST);

                return responseEntity;
            }
            //Si no hay errores persistimos el cliente,
            //comprobando previamente si nos han enviado una imagen o archivo 

            if(!file.isEmpty()){
               String fileCode = fileuploadUtil.saveFile(file.getOriginalFilename(), file);
               cliente.setImagenCliente(fileCode+"-"+file.getOriginalFilename()); 
               
               //Devolver info respecto a file recibid

               FileUploadResponse fileUploadResponse = FileUploadResponse
                            .builder()
                            .fileName(fileCode + "-" + file.getOriginalFilename())
                            .downloadURI("/clientes/downloadFile/"
                                +fileCode + "-" + file.getOriginalFilename())
                            .size(file.getSize())
                            .build();

                responseAsMap.put("info de la imagen: ", fileUploadResponse);     

            }
            Cliente clienteDB = clienteService.save(cliente);
           
            try{
            if(clienteDB != null){
                String mensaje ="el cliente se ha creado correctamente";
                responseAsMap.put("mensaje", mensaje);
                responseAsMap.put("cliente", clienteDB);
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.OK);
            } else{
                //No se ha creado el producto
            }
            }catch(DataAccessException e){
                String errorGrave = "Ha tenido lugar un error grave" + ", y la causa mas probable puede ser"+
                                      e.getMostSpecificCause();
                responseAsMap.put("error grave", errorGrave);
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        return responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.CREATED);
    }

    /**
     * El método siguiente ACTUALIZA un producto en la base de datos
     */
    // recibe en el cuerpo un JSON que es el producto q queremos dar de alta. EL
    // producto viene dentro de la peticion cuando es post
    // si es get viene el producto en la cabecera. El metodo insert recibe en el
    // cuerpo de la peticion @Requestbody el producto
    // Se valida el producto que ha llegado con @Valid (tiene q cumplir con los
    // requisitos puestos en la entidad)
    // BindingResult:
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> update(@Valid @RequestBody Cliente cliente,
            BindingResult result,
            @PathVariable(name = "id") Integer id) {

        Map<String, Object> responseAsMap = new HashMap<>();

        ResponseEntity<Map<String, Object>> responseEntity = null;

        // Primero comprobar si hay errores en el cliente recibido:
        // GetAllErrors me da los errores, pero no un string si no un objeto
        if (result.hasErrors()) {
            List<String> errorMessages = new ArrayList<>();

            for (ObjectError error : result.getAllErrors()) {
                errorMessages.add(error.getDefaultMessage());
            }

            responseAsMap.put("errores", errorMessages);

            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.BAD_REQUEST);

            return responseEntity;
        }
        // Si no hay errores actualizamos el cliente
        // Vinculando previamente el id que se recibe con el cliente
        cliente.setId(id);
        Cliente clienteDB = clienteService.save(cliente);

        try {
            if (clienteDB != null) {
                String mensaje = "el cliente se ha actualizado correctamente";
                responseAsMap.put("mensaje", mensaje);
                responseAsMap.put("cliente", clienteDB);
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.OK);
            } else {
                // No se ha actualizado el cliente
            }
        } catch (DataAccessException e) {
            String errorGrave = "Ha tenido lugar un error grave" + ", y la causa mas probable puede ser" +
                    e.getMostSpecificCause();
            responseAsMap.put("error grave", errorGrave);
            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }

    /**
     * Método que permite borrar cliente
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<String> delete(@PathVariable(name = "id") int id) {

        ResponseEntity<String> responseEntity = null;

        try {
            // Primero lo recuperamos
            Cliente cliente = clienteService.findById(id);

            if (cliente != null) {
                clienteService.delete(cliente);
                responseEntity = new ResponseEntity<String>("borrado con exito", HttpStatus.OK);
            } else {
                responseEntity = new ResponseEntity<String>("no existe el cliente", HttpStatus.NOT_FOUND);
            }

        } catch (DataAccessException e) {
            e.getMostSpecificCause();
            responseEntity = new ResponseEntity<String>("error fatal", HttpStatus.INTERNAL_SERVER_ERROR);

        }
        return responseEntity;

    }

    /**
     * Implementa filedownnload end point API
     **/ // esto seria un end point
    @GetMapping("/downloadFile/{fileCode}")
    public ResponseEntity<?> downloadFile(@PathVariable(name = "fileCode") String fileCode) {

        Resource resource = null;

        try {
            resource = fileDownloadUtil.getFileAsResource(fileCode);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        if (resource == null) {
            return new ResponseEntity<>("File not found ", HttpStatus.NOT_FOUND);
        }

        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);

    }

}
