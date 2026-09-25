# basketball-event-contracts

Fuente de verdad de los contratos Kafka/Avro compartidos por los microservicios **Basketball Stats**.

## Artefacto Maven

```xml
<dependency>
  <groupId>com.fernandez.basketball</groupId>
  <artifactId>basketball-event-contracts</artifactId>
  <version>1.1.0</version>
</dependency>
```

El artefacto se publica en GitHub Packages y contiene las clases Java generadas desde `src/main/avro`.

### FileEvent

`FileEventValue` incluye:

- `fileType`
- `filePath`
- `expectedRows` opcional (`null` por defecto), usado por flujos que conocen de antemano el número esperado de filas, como POINT_BY_POINT.

Añadir `expectedRows` es backward-compatible porque es nullable y tiene default `null`.

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

Jira: KAN-17 / KAN-84.
