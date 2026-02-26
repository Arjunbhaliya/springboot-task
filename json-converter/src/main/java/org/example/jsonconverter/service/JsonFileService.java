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

        if (root.isArray()) {
            for (JsonNode objectNode : root) {
                Map<String, String> flatMap = new LinkedHashMap<>();
                flattenJson("", objectNode, flatMap);
                rows.add(flatMap);
            }
        } else if (root.isObject()) {
            boolean multiElement = true;
            for (JsonNode element : root) {
                if (!element.isObject()) {
                    multiElement = false;
                    break;
                }
            }
            if (multiElement) {
                root.properties().forEach(entry -> {
                    Map<String, String> flatMap = new LinkedHashMap<>();
                    flattenJson("", entry.getValue(), flatMap);
                    rows.add(flatMap);
                });
            } else {
                Map<String, String> flatMap = new LinkedHashMap<>();
                flattenJson("", root, flatMap);
                rows.add(flatMap);
            }
        }

        String csvName = fileName.replace(".json", ".csv");
        Path outputPath = Paths.get("storage/output/" + csvName);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
//            writer.write(String.join(",", flatMap.keySet()));
//            writer.newLine();
//            writer.write(String.join(",", flatMap.values()));
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

//        try (BufferedWriter writer = Files.newBufferedWriter(outputPath);
//             CSVWriter csvWriter = new CSVWriter(writer)) {
//            String[] headers = flatMap.keySet().toArray(new String[0]);
//            csvWriter.writeNext(headers);
//            String[] values = flatMap.values().toArray(String[]::new);
//            csvWriter.writeNext(values);
//        }

            JsonFile json = jsonFileRepository.findByFileName(fileName);
            if (json != null) {
                json.setStatus("PROCESSED");
                jsonFileRepository.save(json);
            }
        }
    }

    private void flattenJson(String prefix, JsonNode node, Map<String, String> result) {
        if (node.isNull()) {
            result.put(prefix, "");
        } else if (node.isObject()) {
            if (node.isEmpty()) {
                result.put(prefix, "");
            }
            node.properties().forEach(entry -> {
                String newPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "_" + entry.getKey();
                flattenJson(newPrefix, entry.getValue(), result);
            });
        } else if (node.isArray()) {
            if (node.isEmpty()) {
                result.put(prefix, "");
            } else {
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
                        String newPrefix = prefix + ".";
                        flattenJson(newPrefix, element, result);
                    }
                } else {
                    StringBuilder val = new StringBuilder();
                    for (int i = 0; i < node.size(); i++) {
                        if (i > 0) val.append("|");
                        val.append(node.get(i).asText());
                    }
                    result.put(prefix, val.toString());
                }
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