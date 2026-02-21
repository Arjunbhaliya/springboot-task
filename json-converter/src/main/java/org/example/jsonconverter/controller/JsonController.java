package org.example.jsonconverter.controller;

import lombok.RequiredArgsConstructor;
import org.example.jsonconverter.service.JsonFileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
@RequestMapping("/json")
@RequiredArgsConstructor
public class JsonController {

    public final JsonFileService jsonFileService;

    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile uploadedJson) throws IOException {
        jsonFileService.upload(uploadedJson);
        return ResponseEntity.ok("File uploaded successfully");
    }

    @PostMapping("/convert/{fileName}")
    public ResponseEntity<String> convert(@PathVariable String fileName) {
        try {
            jsonFileService.convertJsonToCsv(fileName);
            return ResponseEntity.ok("Converted to CSV successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable String fileName) throws FileNotFoundException {
        Resource resource = jsonFileService.download(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\" ")
                .body(resource);
    }

}
