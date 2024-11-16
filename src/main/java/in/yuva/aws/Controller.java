package in.yuva.aws;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping({"/api"})
@RestController
@Log4j2
public class Controller {

    @Autowired
    private Service service;

    @GetMapping({"/up"})
    public ResponseEntity<String> health() {
        log.info("{} :: controller method :: checking api up",
                Controller.class.getSimpleName());
        try {
            String response = service.getBuckets();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("{} :: controller method :: error in checking api",
                    Controller.class.getSimpleName(), e);
            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }

    @PostMapping(
            value = {"/upload"},
            consumes = {"multipart/form-data"}
    )
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {
        log.info("{} :: controller method :: uploading file {}", Controller.class.getSimpleName(), file.getOriginalFilename());
        return service.upload();
    }

    @PostMapping({"/create"})
    public String upload() {
        return service.upload();
    }
}
