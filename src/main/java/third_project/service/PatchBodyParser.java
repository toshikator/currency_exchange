package third_project.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class PatchBodyParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PatchBodyParser() {
        // utility class
    }

    /**
     * Main entry point — detects content type automatically.
     */
    public static ParsedBody parse(HttpServletRequest request) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String body = readBody(request);

        String contentType = request.getContentType();
        if (contentType == null || contentType.isBlank()) {
            return new ParsedBody(body, Map.of(), Map.of());
        }

        // Cut off ";charset=UTF-8" etc.
        int semicolonIndex = contentType.indexOf(';');
        if (semicolonIndex != -1) {
            contentType = contentType.substring(0, semicolonIndex).trim();
        }

        switch (contentType) {
            case "application/x-www-form-urlencoded": {
                Map<String, List<String>> form = parseUrlEncoded(body);
                return new ParsedBody(body, form, Map.of());
            }
            case "application/json": {
                Map<String, Object> json = parseJson(body);
                return new ParsedBody(body, Map.of(), json);
            }
            default:
                // Unsupported → return raw only
                return new ParsedBody(body, Map.of(), Map.of());
        }
    }

    /**
     * Read raw request body as String.
     */
    private static String readBody(HttpServletRequest request) throws IOException {
        try (BufferedReader reader = request.getReader();
             StringWriter writer = new StringWriter()) {

            // Java 9+ method, работает и в 21
            reader.transferTo(writer);
            return writer.toString();
        }
    }

    /**
     * Parse application/x-www-form-urlencoded body manually.
     */
    private static Map<String, List<String>> parseUrlEncoded(String form) {
        if (form == null || form.isEmpty()) {
            return Map.of();
        }

        Map<String, List<String>> map = new HashMap<>();
        String[] pairs = form.split("&");

        for (String pair : pairs) {
            if (pair.isEmpty()) continue;

            String[] parts = pair.split("=", 2);

            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = (parts.length > 1)
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                    : "";

            map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return map;
    }

    /**
     * Parse JSON using Jackson.
     */
    private static Map<String, Object> parseJson(String json) throws IOException {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        return MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Result wrapper object – record is fully supported in Java 21.
     */
    public record ParsedBody(
            String raw,
            Map<String, List<String>> form,   // For x-www-form-urlencoded
            Map<String, Object> json          // For JSON
    ) {
        public String getFirst(String key) {
            List<String> list = form.get(key);
            if (list == null || list.isEmpty()) return null;
            // без List.getFirst() — чтобы не упираться в совсем свежие методы коллекций
            return list.get(0);
        }

        public List<String> getAll(String key) {
            return form.getOrDefault(key, List.of());
        }
    }
}
