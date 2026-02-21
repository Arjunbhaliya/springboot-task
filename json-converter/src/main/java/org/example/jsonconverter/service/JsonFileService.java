package org.example.jsonconverter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.jsonconverter.entity.JsonFile;
import org.example.jsonconverter.repository.JsonFileRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JsonFileService {

    public final JsonFileRepository jsonFileRepository;

    public void upload(MultipartFile uploadedJson) throws IOException {
        if (uploadedJson.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.readTree(uploadedJson.getInputStream());
        } catch (IOException e) {
            throw new IllegalArgumentException("File is not valid Json ");
        }

        String name = uploadedJson.getOriginalFilename();
        Path path = Paths.get("storage/input/" + name);
        Files.copy(uploadedJson.getInputStream(), path);

        JsonFile json = new JsonFile();
        json.setFileName(name);
        json.setStatus("UPLOADED");
        jsonFileRepository.save(json);
    }

    public void convertJsonToCsv(String fileName) throws IOException {

        Path inputPath = Paths.get("storage/input/" + fileName);
        if (!Files.exists(inputPath)) {
            throw new IllegalArgumentException("JSON file not found");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(inputPath.toFile());

        Map<String, String> flatMap = new LinkedHashMap<>();
        flattenJson("", root, flatMap);

        String csvName = fileName.replace(".json", ".csv");
        Path outputPath = Paths.get("storage/output/" + csvName);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write(String.join(",", flatMap.keySet()));
            writer.newLine();
            writer.write(String.join(",", flatMap.values()));
        }

        JsonFile json = jsonFileRepository.findByFileName(fileName);
        if (json != null) {
            json.setStatus("PROCESSED");
            jsonFileRepository.save(json);
        }
    }

    private void flattenJson(String prefix, JsonNode node, Map<String, String> result) {
        if (node.isObject()) {
            node.properties().forEach(entry -> {
                String newPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                flattenJson(newPrefix, entry.getValue(), result);
            });
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                flattenJson(prefix + "[" + i + "]", node.get(i), result);
            }
        } else {
            result.put(prefix, node.asText());
        }
    }


    public FileSystemResource download(String fileName) throws FileNotFoundException {
        Path filepath = Paths.get("storage/output/" + fileName);
        if (Files.exists(filepath)) {
            return new FileSystemResource(filepath);
        } else {
            throw new FileNotFoundException("File is not exists");
        }

    }
}
