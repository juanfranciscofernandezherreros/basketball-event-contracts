# Changelog

## 1.6.0

- [minor] Adds the compatible `TeamStatsRejectedKey` and `TeamStatsRejectedValue` Avro contracts for functional rejections on `team-stats.rejected`.

## 1.5.1 - 2026-10-04

- [patch] Sincroniza la versión del artefacto, documentación, contrato Avro y pruebas a `1.5.1`, manteniendo el inventario de consumidores CSV.

## 1.5.0 - 2026-10-01

- [minor] KAN-261 añade `PersistedEventKey` y `PersistedEventValue` como contrato canónico para eventos `*.persisted.success`.
- [minor] El contrato conserva trazabilidad mediante `sourceEventId`, admite `importId` y `persistedRecords` opcionales e incluye dataset, timestamp de persistencia y versión de contrato.
- [minor] Documenta los seis topics `*.persisted.success` y mantiene las DLT existentes como única salida de error definitivo.
- [minor] Añade pruebas de parsing, campos requeridos y round-trip de serialización Avro.

## 1.4.0 - 2026-09-28

- [minor] KAN-39 añade `sourceEventId` opcional a `FixtureValue`.
- [minor] El nuevo campo es nullable y con default `null` para mantener compatibilidad hacia atrás.
- [minor] Habilita trazabilidad de FIXTURES desde el evento de ingestión hasta persistence.


## 1.3.0 - 2026-09-28

- [minor] KAN-28 añade `MatchResultValue` como contrato canónico de `results.parsed`.
- [minor] Incluye `sourceEventId` para preservar trazabilidad e idempotencia entre parser y persistence.

## 1.2.0 - 2026-09-25

- [minor] KAN-75 añade `contractVersion` opcional a `FileEventValue` para trazabilidad de contrato.
- [minor] KAN-75 añade `routedAtEpochMillis` opcional para registrar el momento de paso por el router.
- [minor] Ambos campos son backward-compatible: nullable y con default `null`.


## 1.1.0 - 2026-09-25

- [minor] KAN-84 añade `expectedRows` opcional a `FileEventValue`, con default `null`.
- [minor] Mantiene compatibilidad hacia atrás para consumidores que solo usan `fileType` y `filePath`.
- [minor] Añade una prueba explícita del contrato opcional requerido por POINT_BY_POINT.

## 1.0.2 - 2026-09-25

- [patch] Ejecuta publicación y limpieza después de completar el workflow `auto-merge`, evitando depender de eventos `push` suprimidos por `GITHUB_TOKEN`.
- [patch] Publica el artefacto Maven desde `main` y elimina/verifica la rama bootstrap pendiente.

## 1.0.1 - 2026-09-25

- [patch] Publica automáticamente el artefacto Maven en GitHub Packages al integrar cambios en `main`.
- [patch] Añade limpieza de la rama inicial de KAN-17 que quedó tras el primer merge manual.

## 1.0.0 - 2026-09-25

- [major] KAN-17 crea la fuente de verdad de contratos Kafka/Avro de Basketball Stats.
- [major] Centraliza FileEvent, RESULTS, FIXTURES, MATCH, PLAYER, POINT-BY-POINT y TEAM-STATS.
- [major] Genera clases Java desde Avro y publica el JAR mediante GitHub Packages.
- [major] Añade validación CI de parsing, nombres Avro únicos y compatibilidad hacia atrás respecto a la rama base.
