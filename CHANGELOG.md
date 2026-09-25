# Changelog

## 1.0.1 - 2026-09-25

- [patch] Publica automáticamente el artefacto Maven en GitHub Packages al integrar cambios en `main`.
- [patch] Añade limpieza de la rama inicial de KAN-17 que quedó tras el primer merge manual.


## 1.0.0 - 2026-09-25

- [major] KAN-17 crea la fuente de verdad de contratos Kafka/Avro de Basketball Stats.
- [major] Centraliza FileEvent, RESULTS, FIXTURES, MATCH, PLAYER, POINT-BY-POINT y TEAM-STATS.
- [major] Genera clases Java desde Avro y publica el JAR mediante GitHub Packages.
- [major] Añade validación CI de parsing, nombres Avro únicos y compatibilidad hacia atrás respecto a la rama base.
