package in.yuva.aws;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@RequestMapping(ENDPOINT.API)
@RestController
@Log4j2
public class Controller {

    @Autowired
    private Service service;

    @GetMapping(ENDPOINT.UP)
    public ResponseEntity<String> health() {
        log.info("{} :: health method :: checking api up",
                Controller.class.getSimpleName());
        try {
            String response = service.getBuckets();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("{} :: health method :: error in checking api {}",
                    Controller.class.getSimpleName(), e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }

    @PostMapping(
            value = {ENDPOINT.UPLOAD},
            consumes = {"multipart/form-data"}
    )
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        log.info("{} :: upload method :: uploading file {}",
                Controller.class.getSimpleName(), file.getOriginalFilename());
        String response = service.uploadFileToS3(file);
        if(response.contains("successfully"))
            return ResponseEntity.ok()
                .body(response);
        else
            return ResponseEntity.internalServerError()
            .body(response);
    }

    @GetMapping(ENDPOINT.DOWNLOAD)
    public ResponseEntity<String> download(@RequestParam("file") String filename) throws IOException {
        log.info("{} :: download method :: downloading file {}",
                Controller.class.getSimpleName(), filename);
        String folderName = LocalDate.now().toString() + "/";
        String response = service.getFileFromS3(filename,folderName);
        if(response.contains("successfully"))
            return ResponseEntity.ok()
                    .body(response);
        else
            return ResponseEntity.internalServerError()
                    .body(response);
    }

    @DeleteMapping(ENDPOINT.DELETE)
    public ResponseEntity<String> delete(@RequestParam("file") String filename){
        log.info("{} :: delete method :: deleting file {}",
                Controller.class.getSimpleName(), filename);
        String folderName = LocalDate.now().toString() + "/";
        String response = service.deleteFromS3(filename,folderName);
        if(response.contains("successfully"))
            return ResponseEntity.ok()
                    .body(response);
        else
            return ResponseEntity.internalServerError()
                    .body(response);
    }

}
