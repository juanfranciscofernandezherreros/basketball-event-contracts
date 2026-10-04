# AGENTS.md

Estas reglas son obligatorias para cualquier agente, asistente o automatización que modifique este repositorio.

## Pre-flight obligatorio

La primera operación de lectura del repositorio en cada tarea o sesión debe ser abrir y leer completamente este `AGENTS.md` desde la rama por defecto. Si referencia otras reglas, deben leerse antes de cualquier escritura. No cuenta una lectura de otra conversación, sesión o tarea.

Está prohibida cualquier operación de escritura antes de completar este pre-flight.

## Autonomía sin bloqueos

Tras leer las reglas, el agente continúa de forma autónoma: elige una rama descriptiva, determina el nivel SemVer según el impacto real y documenta ambas decisiones en la PR. No debe pedir confirmaciones intermedias salvo petición expresa del usuario.

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

## Propagación obligatoria a consumidores CSV

Este repositorio es el inventario canónico de los consumidores de
`com.fernandez.basketball:basketball-event-contracts`. Al publicar una versión
nueva, se debe actualizar **la misma versión** en todos estos repositorios:

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

`csv-processing-core` no declara este artefacto y queda fuera del ciclo.

### Regla de publicación

Una release de contratos no está finalizada hasta que cada consumidor de la
lista haya seguido este ciclo: crear rama con la clave Jira, actualizar la
dependencia y su versión patch, actualizar `CHANGELOG.md` y documentación,
ejecutar sus pruebas con Java 21, abrir PR, comprobar los checks del SHA actual,
fusionar y eliminar la rama origen. Antes de cerrar la tarea de contratos se
debe verificar `origin/main` de cada repositorio, nunca solo una rama local.

La automatización descrita arriba está implementada en
`.github/workflows/propagate-contract-version.yml`; su configuración se detalla
en la sección siguiente.

### Automatización GitHub Actions

`.github/workflows/propagate-contract-version.yml` se ejecuta después de que
termine correctamente el flujo de publicación y también admite ejecución manual. Para cada
consumidor crea una tarea Jira, actualiza dependencia, versión patch, README y
CHANGELOG, ejecuta pruebas con Java 21, abre PR, espera los checks, fusiona y
cierra Jira. Si falta la dependencia en un repositorio, el job falla para
señalar que su migración inicial debe integrarse primero.

Configurar estos secretos en el repositorio de contratos:

- `CONTRACTS_SYNC_TOKEN`: fine-grained PAT con acceso de escritura a Contents y
  Pull Requests, y lectura de Packages en los quince repositorios.
- `JIRA_BASE_URL`, `JIRA_EMAIL` y `JIRA_API_TOKEN`: sitio y credenciales de una
  cuenta que pueda crear y transicionar tareas en el proyecto Jira `KAN`.

La ejecución manual acepta una versión publicada; vacía, usa `<revision>` de
`pom.xml`.

## Versionado

- `patch`: corrección compatible.
- `minor`: nuevo contrato/campo compatible.
- `major`: cambio incompatible.

## Tests

Baseline Java: JDK 21. Ejecutar siempre `./mvn-java.ps1 -B test`; el lanzador
lee este archivo y selecciona el JDK únicamente para el proceso Maven, sin
modificar `JAVA_HOME` global.
