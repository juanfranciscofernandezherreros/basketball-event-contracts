# basketball-event-contracts

Fuente de verdad de los contratos Kafka/Avro compartidos por los microservicios **Basketball Stats**.

## Artefacto Maven

```xml
<dependency>
  <groupId>com.fernandez.basketball</groupId>
  <artifactId>basketball-event-contracts</artifactId>
  <version>1.5.0</version>
</dependency>
```

El artefacto se publica en GitHub Packages y contiene las clases Java generadas desde `src/main/avro`.

### Persisted success

`PersistedEventKey` y `PersistedEventValue` forman el contrato común para confirmar que una unidad de importación terminó correctamente su persistencia en PostgreSQL.

La key usa `sourceEventId`. El value incluye:

- `sourceEventId`: trazabilidad con el evento de origen.
- `importId`: identificador opcional de importación cuando el flujo lo tenga.
- `datasetType`: tipo lógico del dataset persistido.
- `persistedAtEpochMillis`: instante UTC epoch-millis de finalización.
- `persistedRecords`: número opcional de registros persistidos.
- `contractVersion`: versión del contrato, con default `1.5.0`.

Topics canónicos de éxito:

- `fixtures.persisted.success`
- `results.persisted.success`
- `point-by-point.persisted.success`
- `stats-match.persisted.success`
- `stats-player.persisted.success`
- `team-stats.persisted.success`

Las DLT existentes siguen representando el error definitivo después de retries agotados. No se define un `*.persisted.error` para evitar duplicar esa semántica.

### FIXTURES

`FixtureValue` incluye `sourceEventId` opcional para preservar trazabilidad desde el evento `file.ready.fixtures` hasta PostgreSQL. El campo es nullable y tiene default `null`, por lo que lectores nuevos siguen siendo compatibles con mensajes FIXTURES anteriores.

### RESULTS

`MatchResultValue` es el value canónico de `results.parsed`. Incluye `sourceEventId`
para trazabilidad e idempotencia, junto con el identificador del partido, marcador,
parciales y metadatos de competición.

### FileEvent

`FileEventValue` incluye:

- `fileType`
- `filePath`
- `expectedRows` opcional (`null` por defecto), usado por flujos que conocen de antemano el número esperado de filas, como POINT_BY_POINT.
- `contractVersion` opcional (`null` por defecto), rellenado por el router para identificar la versión común aplicada al evento.
- `routedAtEpochMillis` opcional (`null` por defecto), timestamp UTC epoch-millis del paso por el router.

Los campos opcionales usan unión con `null` y default `null`, por lo que la evolución sigue siendo backward-compatible con consumidores anteriores.

## Evolución de contratos

- Cambios compatibles hacia atrás: MINOR o PATCH según impacto.
- Cambios incompatibles: MAJOR.
- En Pull Requests, CI compara los schemas actuales con los de la rama base mediante `SchemaCompatibility`.
- Un contrato compartido no debe copiarse manualmente a un microservicio una vez este haya sido migrado al artefacto común.

## Desarrollo

Baseline JDK 21.

```bash
mvn -B test
```

Jira: KAN-17 / KAN-84 / KAN-75 / KAN-261.
