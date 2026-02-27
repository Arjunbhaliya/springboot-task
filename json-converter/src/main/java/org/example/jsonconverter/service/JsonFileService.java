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
import java.util.*;

import static org.apache.commons.text.StringEscapeUtils.escapeCsv;

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

        List<Map<String, String>> rows = new ArrayList<>();
        Map<String, String> result = new LinkedHashMap<>();

        flattenJson("", root, result, rows);

        if (rows.isEmpty()) {
            rows.add(result);
        }

        String csvName = fileName.replace(".json", ".csv");
        Path outputPath = Paths.get("storage/output/" + csvName);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            Set<String> headerSet = new LinkedHashSet<>();

            for (Map<String, String> row : rows) {
                headerSet.addAll(row.keySet());
            }
            writer.write(String.join(",", headerSet));
            writer.newLine();

            for (Map<String, String> row : rows) {
                List<String> rowValues = new ArrayList<>();
                for (String header : headerSet) {
                    String value = row.getOrDefault(header, "");
                    rowValues.add(escapeCsv(value));
                }
                writer.write(String.join(",", rowValues));
                writer.newLine();
            }

            JsonFile json = jsonFileRepository.findByFileName(fileName);
            if (json != null) {
                json.setStatus("PROCESSED");
                jsonFileRepository.save(json);
            }
        }
    }

    private void flattenJson(String prefix, JsonNode node, Map<String, String> result, List<Map<String, String>> rows) {

        switch (node.getNodeType()) {

            case NULL -> result.put(prefix, "");

            case STRING, NUMBER, BOOLEAN -> result.put(prefix, node.asText());

            case OBJECT -> handleObject(prefix, node, result, rows);

            case ARRAY -> handleArray(prefix, node, result, rows);
        }
    }

    private void handleArray(String prefix, JsonNode node, Map<String, String> result, List<Map<String, String>> rows) {

        if (node.isEmpty()) {
            result.put(prefix, "");
            return;
        }

        boolean nonPrimitive = false;
        for (JsonNode element : node) {
            if (element.isObject() || element.isArray()) {
                nonPrimitive = true;
                break;
            }
        }

        if (nonPrimitive) {
            for (int i = 0; i < node.size(); i++) {
                JsonNode element = node.get(i);
                Map<String, String> newResult = new LinkedHashMap<>(result);
                int sizeBefore = newResult.size();
                flattenJson(prefix, element, newResult, rows);
                if (newResult.size() > sizeBefore) {
                    rows.add(newResult);
                }
            }
            return;
        }
            StringBuilder val = new StringBuilder();
            for (int i = 0; i < node.size(); i++) {
                if (i > 0) val.append("|");
                val.append(node.get(i).asText());
            }
            result.put(prefix, val.toString());

    }


    private void handleObject(String prefix, JsonNode node, Map<String, String> result, List<Map<String, String>> rows) {
        if (node.isEmpty()) {
            result.put(prefix, "");
            return;
        }

        boolean multiElement = true;
        for (JsonNode element : node) {
            if (!element.isObject()) {
                multiElement = false;
                break;
            }
        }

        if (multiElement && node.size() > 1) {
            node.properties().forEach(entry -> {
                Map<String, String> newResult = new LinkedHashMap<>(result);
                flattenJson(prefix, entry.getValue(), newResult, rows);
                rows.add(newResult);
            });
            return;
        }
        node.properties().forEach(entry -> {
            String newPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            flattenJson(newPrefix, entry.getValue(), result, rows);
        });
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