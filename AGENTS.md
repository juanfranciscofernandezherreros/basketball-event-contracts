# AGENTS.md

Estas reglas son obligatorias para cualquier agente, asistente o automatización que modifique este repositorio.

## Pre-flight obligatorio

La primera operación de lectura del repositorio en cada tarea o sesión debe ser abrir y leer completamente este `AGENTS.md` desde la rama por defecto. Si referencia otras reglas, deben leerse antes de cualquier escritura. No cuenta una lectura de otra conversación, sesión o tarea.

Está prohibida cualquier operación de escritura antes de completar este pre-flight.

## Jira obligatorio: primer gate de trabajo

Cada cambio requiere una tarea Jira antes de crear una rama o modificar archivos.
El agente debe buscar primero una tarea existente y, si no la hay, crear una.
Debe moverla a `En curso` antes de cualquier escritura y usar su clave en la
rama, los commits, la Pull Request y los comentarios de progreso.

Flujo obligatorio:

1. Leer este `AGENTS.md` y sus reglas referenciadas.
2. Buscar o crear la tarea Jira exacta y moverla a `En curso`.
3. Partir de `main` actualizado y crear una rama con la clave Jira.
4. Determinar SemVer; actualizar código, pruebas, `pom.xml` y `CHANGELOG.md`.
5. Ejecutar las validaciones aplicables, como mínimo `mvn -B test`.
6. Crear o actualizar automáticamente la Pull Request y corregir sus checks.
7. Fusionar automáticamente cuando el SHA actual esté en verde y no haya bloqueos.
8. Eliminar la rama origen, verificar su eliminación y comentar la evidencia en Jira.
9. Mover la tarea Jira a `Finalizado` solo tras completar los pasos anteriores.

## Autonomía sin bloqueos

Tras leer las reglas, el agente continúa de forma autónoma: elige una rama descriptiva, determina el nivel SemVer según el impacto real y documenta ambas decisiones en la PR. No debe pedir confirmaciones intermedias salvo petición expresa del usuario.

### Pull Request y merge automáticos

El agente debe crear automáticamente la Pull Request al terminar los cambios,
vigilar los checks requeridos sobre el SHA actual y corregir los fallos en la
misma rama. Cuando todos los checks aplicables estén en verde y GitHub no
indique bloqueos, debe fusionarla automáticamente, eliminar la rama origen y
verificar que la rama remota ya no existe. No debe pedir confirmación adicional
para crear la PR, hacer merge o eliminar la rama después de un merge correcto.

### Entrega automática obligatoria

Antes de crear la PR, el agente debe comprobar y sincronizar `pom.xml` y
`CHANGELOG.md` con la versión propuesta. Las referencias a la versión actual en
`README.md` las sincroniza automáticamente el workflow de publicación: al
publicar un contrato, compara el README con `<revision>` de `pom.xml` y abre
una PR de documentación solo si hay diferencias. No se deben corregir esas
referencias a mano ni incluirlas en la PR del contrato. La PR automática debe
pasar CI y fusionarse por el flujo habitual; su merge no debe volver a publicar
el artefacto. Tras el merge, el agente debe continuar automáticamente con los
consumidores afectados por el contrato:
actualizar dependencias, implementar el uso del nuevo contrato, crear los
topics/configuración necesarios, validar el flujo de extremo a extremo y dejar
trazabilidad Jira. No debe declarar la tarea completa hasta cerrar esas acciones
o crear y enlazar las tareas Jira necesarias para los pasos posteriores.

## Prohibición absoluta de escritura directa en `main`

Ningún cambio puede escribirse, commitearse ni pushearse directamente a `main`.

La secuencia operativa obligatoria está definida en **Jira obligatorio: primer
gate de trabajo**. El trabajo no termina hasta completar merge, limpieza de la
rama y cierre de Jira.

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
