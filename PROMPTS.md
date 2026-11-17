# PROMPTS.md – Fase 2 con IA (rama `mejora-ia`)

- **Proyecto:** CampusGPS (Lab 13 · Programación en Móviles · Tecsup)
- **Herramienta:** Gemini en Android Studio
- **Fecha:** 2026-11-17
- **Regla aplicada:** la IA propone, yo decido. Revisé cada cambio contra MVVM + Repository antes de hacer commit.

## Registro

| # | Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué | Qué aprendí |
|---|---|---|---|---|---|
| 1 | «Agrega un filtro por categoría con FilterChip (Estudio, Comida, Eventos, Servicios) que muestre solo esos lugares en el mapa y en la lista. El estado vive en MapaViewModel, los Composables no filtran, el ViewModel no recibe Context y masCercano sale de la lista completa.» | Agregó `categoriaSeleccionada` (StateFlow) y `seleccionarCategoria()` en el ViewModel, derivó `lugares` con `combine` + `stateIn` y puso una fila de FilterChip con scroll en PantallaMapa. Compiló. | 1) Quité la lista `listOf("Estudio", "Comida", ...)` escrita en el Composable; ahora el ViewModel expone `categorias` sacadas del repositorio. 2) `masCercano` ahora se deriva de la lista **filtrada** y no de la completa. | 1) Las categorías son datos del dominio: dentro de la vista rompen MVVM y no se actualizan si cambia el repositorio. 2) La regla de mi propio prompt estaba mal: al filtrar desaparecía el marcador verde y "Ir al más cercano" no encontraba el lugar en el mapa. | Un prompt con una regla mal pensada produce código que compila pero se comporta mal; hay que probar cada caso. |
| 2 | «Agrega un selector de radio de 100, 200 o 500 m que dibuje un Circle alrededor de mi posición y marque en la lista los lugares dentro del radio. El radio vive en el ViewModel, el ViewModel expone un Set con los ids dentro del radio, los radios disponibles los expone el ViewModel y los Composables no comparan distancias.» | Agregó `radioSeleccionado`, `seleccionarRadio()`, `radiosDisponibles` y `lugaresEnRadio` (con `combine`) en el ViewModel; `Circle` en MapaUbicacion; etiqueta "Dentro del radio" en ListaLugares; fila de chips en PantallaMapa. Compiló. | Solo cambié la etiqueta del chip de `100m` a `100 m`. | Lo demás respetaba MVVM: la comparación `metros <= radio` está en el ViewModel y la vista solo dibuja. El espacio es para que coincida con el formato de distancias de la lista (`88 m`). | (Si el prompt deja claras las reglas de MVVM, la IA las respeta y solo hay que pulir detalles.) |
| 3 | «Activa el punto azul nativo (isMyLocationEnabled) y el botón de mi ubicación solo si el permiso fue concedido. Es un cambio solo de la vista; MapaUbicacion recibe un Boolean y no revisa permisos; PantallaMapa calcula el permiso con checkSelfPermission.» | Agregó el parámetro `mostrarMiUbicacion` en MapaUbicacion con `MapProperties` y `MapUiSettings`, y en PantallaMapa calculó el permiso antes del mapa. Compiló. | La comprobación de permiso quedó duplicada (en `alPulsar()` y antes del mapa). La moví a una sola función `tienePermisoUbicacion()` que usan ambos. | Evitar código repetido: si mañana cambia la forma de revisar el permiso, se cambia en un solo lugar. El ViewModel no se tocó, como pedía la mejora. | (La IA resuelve lo pedido pero puede duplicar código; revisar que no se repita lógica es parte de mi trabajo.) |

## Resultado
| Mejora | Capas que cambian | Commit |
|---|---|---|
| Filtrar por categoría | ViewModel + View | `feat: agrega filtro por categoria con chips` |
| Radio de cercanía | ViewModel + View | `feat: agrega selector de radio de cercania con circulo en el mapa` |
| Punto azul nativo | View | `feat: activa punto azul nativo y boton de mi ubicacion` |

Dos de las tres mejoras cambian el ViewModel, como pide la guía (mínimo una). En ningún prompt pegué la clave de API.