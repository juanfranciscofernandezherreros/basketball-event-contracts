# AGENTS.md

Estas reglas son obligatorias para cualquier agente, asistente o automatización que modifique este repositorio.

## Pre-flight obligatorio

La primera operación de lectura del repositorio en cada tarea o sesión debe ser abrir y leer completamente este `AGENTS.md` desde la rama por defecto. Si referencia otras reglas, deben leerse antes de cualquier escritura. No cuenta una lectura de otra conversación, sesión o tarea.

Está prohibida cualquier operación de escritura antes de completar este pre-flight.

## Autonomía sin bloqueos

Tras leer las reglas, el agente continúa de forma autónoma: elige una rama descriptiva, determina el nivel SemVer según el impacto real y documenta ambas decisiones en la PR. No debe pedir confirmaciones intermedias salvo petición expresa del usuario.

### Pull Request y merge automáticos

El agente debe crear automáticamente la Pull Request al terminar los cambios,
vigilar los checks requeridos sobre el SHA actual y corregir los fallos en la
misma rama. Cuando todos los checks aplicables estén en verde y GitHub no
indique bloqueos, debe fusionarla automáticamente, eliminar la rama origen y
verificar que la rama remota ya no existe. No debe pedir confirmación adicional
para crear la PR, hacer merge o eliminar la rama después de un merge correcto.

## Prohibición absoluta de escritura directa en `main`

Ningún cambio puede escribirse, commitearse ni pushearse directamente a `main`.

Toda modificación debe seguir este flujo:

1. Leer `AGENTS.md` y reglas referenciadas.
2. Partir del `main` actualizado.
3. Crear una rama dedicada antes de modificar archivos.
4. Determinar y aplicar el incremento SemVer.
5. Realizar cambios exclusivamente en la rama.
6. Actualizar `CHANGELOG.md`.
7. Mantener README, POM y documentación de versión sincronizados.
8. Ejecutar como mínimo `mvn -B test`.
9. Abrir/actualizar PR hacia `main`.
10. Corregir checks fallidos en la misma rama/PR.
11. Fusionar solo con checks aplicables en verde sobre el SHA actual.
12. Eliminar la rama origen tras merge y verificar su desaparición.

El trabajo no termina hasta completar merge y limpieza.

## Contratos Avro

- Este repositorio es la fuente de verdad de los contratos Kafka/Avro compartidos de Basketball Stats.
- No duplicar manualmente schemas en microservicios una vez migrados.
- Mantener compatibilidad hacia atrás salvo cambio MAJOR explícito.
- Todo cambio incompatible requiere incremento MAJOR y documentación expresa.
- Añadir tests de parsing/compilación de schemas para cada cambio.
- Los microservicios deben consumir una versión publicada de este artefacto, no copiar archivos `.avsc`.

## Inventario de consumidores CSV

Consumidores de `com.fernandez.basketball:basketball-event-contracts`:

- `csv-file-event-router`
- `csv-fixtures-parser`
- `csv-fixtures-persistence`
- `csv-match-normalizer`
- `csv-point-by-point-parser`
- `csv-point-by-point-persistence`
- `csv-results-parser`
- `csv-results-persistence`
- `csv-stats-match-parser`
- `csv-stats-match-persistence`
- `csv-stats-player-parser`
- `csv-stats-player-persistence`
- `csv-team-stats-parser`
- `csv-team-stats-persistence`
- `csv-watcher`

`csv-processing-core` no declara este artefacto y queda fuera del inventario.

## Versionado

- `patch`: corrección compatible.
- `minor`: nuevo contrato/campo compatible.
- `major`: cambio incompatible.

## Tests

Baseline Java: JDK 21. Ejecutar al menos `mvn -B test`.
