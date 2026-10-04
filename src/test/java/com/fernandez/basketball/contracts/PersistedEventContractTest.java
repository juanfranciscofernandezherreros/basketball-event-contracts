package com.fernandez.basketball.contracts;

import com.fernandez.basketball.persistence.PersistedEventKey;
import com.fernandez.basketball.persistence.PersistedEventValue;
import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistedEventContractTest {

    @Test
    void persistedEventSchemasExposeRequiredSuccessMetadata() throws Exception {
        Schema key = new Schema.Parser().parse(Path.of("src/main/avro/PersistedEventKey.avsc").toFile());
        Schema value = new Schema.Parser().parse(Path.of("src/main/avro/PersistedEventValue.avsc").toFile());

        assertEquals("com.fernandez.basketball.persistence.PersistedEventKey", key.getFullName());
        assertEquals(Schema.Type.STRING, key.getField("sourceEventId").schema().getType());

        assertEquals("com.fernandez.basketball.persistence.PersistedEventValue", value.getFullName());
        assertEquals(Schema.Type.STRING, value.getField("sourceEventId").schema().getType());
        assertEquals(Schema.Type.STRING, value.getField("datasetType").schema().getType());
        assertEquals(Schema.Type.LONG, value.getField("persistedAtEpochMillis").schema().getType());

        Schema.Field importId = value.getField("importId");
        assertNotNull(importId);
        assertTrue(importId.schema().getTypes().stream().anyMatch(type -> type.getType() == Schema.Type.NULL));
        assertTrue(importId.schema().getTypes().stream().anyMatch(type -> type.getType() == Schema.Type.STRING));
        assertTrue(importId.hasDefaultValue());

        Schema.Field persistedRecords = value.getField("persistedRecords");
        assertNotNull(persistedRecords);
        assertTrue(persistedRecords.schema().getTypes().stream().anyMatch(type -> type.getType() == Schema.Type.NULL));
        assertTrue(persistedRecords.schema().getTypes().stream().anyMatch(type -> type.getType() == Schema.Type.LONG));
        assertTrue(persistedRecords.hasDefaultValue());

        assertEquals("1.5.1", value.getField("contractVersion").defaultVal().toString());
    }

    @Test
    void persistedEventValueRoundTripsWithGeneratedSpecificRecord() throws Exception {
        PersistedEventKey key = PersistedEventKey.newBuilder()
                .setSourceEventId("source-123")
                .build();

        PersistedEventValue original = PersistedEventValue.newBuilder()
                .setSourceEventId("source-123")
                .setImportId("import-456")
                .setDatasetType("RESULTS")
                .setPersistedAtEpochMillis(1_780_000_000_000L)
                .setPersistedRecords(42L)
                .setContractVersion("1.5.1")
                .build();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        var encoder = EncoderFactory.get().binaryEncoder(output, null);
        new SpecificDatumWriter<PersistedEventValue>(PersistedEventValue.getClassSchema())
                .write(original, encoder);
        encoder.flush();

        var decoder = DecoderFactory.get().binaryDecoder(output.toByteArray(), null);
        PersistedEventValue decoded =
                new SpecificDatumReader<PersistedEventValue>(PersistedEventValue.getClassSchema())
                        .read(null, decoder);

        assertEquals("source-123", key.getSourceEventId());
        assertEquals(original, decoded);
    }
}
