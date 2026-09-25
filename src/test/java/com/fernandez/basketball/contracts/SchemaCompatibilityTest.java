package com.fernandez.basketball.contracts;

import org.apache.avro.Schema;
import org.apache.avro.SchemaCompatibility;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaCompatibilityTest {

    @Test
    void everyCurrentSchemaParsesAndHasUniqueFullName() throws Exception {
        Map<String, Schema> current = loadSchemas(Path.of("src/main/avro"));
        assertTrue(current.size() >= 14, "Expected all shared Basketball Stats contracts");
    }

    @Test
    void fileEventValueSupportsOptionalExpectedRows() throws Exception {
        Map<String, Schema> current = loadSchemas(Path.of("src/main/avro"));
        Schema fileEventValue = current.get("com.example.csvwatcher.watcher.FileEventValue");

        Schema.Field expectedRows = fileEventValue.getField("expectedRows");
        assertTrue(expectedRows != null, "FileEventValue.expectedRows must exist");
        assertEquals(Schema.Type.UNION, expectedRows.schema().getType());
        assertTrue(expectedRows.schema().getTypes().stream().anyMatch(type -> type.getType() == Schema.Type.NULL));
        assertTrue(expectedRows.schema().getTypes().stream().anyMatch(type -> type.getType() == Schema.Type.LONG));
        assertNull(expectedRows.defaultVal());
    }

    @Test
    void currentSchemasRemainBackwardCompatibleWithPullRequestBase() throws Exception {
        String baseDir = System.getenv("BASE_SCHEMA_DIR");
        if (baseDir == null || baseDir.isBlank() || !Files.isDirectory(Path.of(baseDir))) {
            return;
        }

        Map<String, Schema> current = loadSchemas(Path.of("src/main/avro"));
        Map<String, Schema> baseline = loadSchemas(Path.of(baseDir));

        for (Map.Entry<String, Schema> oldEntry : baseline.entrySet()) {
            Schema newSchema = current.get(oldEntry.getKey());
            assertTrue(newSchema != null, "Removed shared contract: " + oldEntry.getKey());

            var compatibility = SchemaCompatibility.checkReaderWriterCompatibility(
                    newSchema,
                    oldEntry.getValue());

            assertEquals(
                    SchemaCompatibility.SchemaCompatibilityType.COMPATIBLE,
                    compatibility.getType(),
                    () -> "Backward-incompatible change in " + oldEntry.getKey() + ": " + compatibility.getDescription());
        }
    }

    private Map<String, Schema> loadSchemas(Path directory) throws IOException {
        Map<String, Schema> schemas = new LinkedHashMap<>();
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".avsc")).sorted().toList()) {
                Schema schema = new Schema.Parser().parse(path.toFile());
                Schema previous = schemas.put(schema.getFullName(), schema);
                assertTrue(previous == null, "Duplicate Avro full name: " + schema.getFullName());
            }
        }
        return schemas;
    }
}
