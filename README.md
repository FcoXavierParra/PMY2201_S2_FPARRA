# Aplicando utilidades de Kotlin – Pizzería

**Actividad:** Semana 2 · Desarrollo App Móviles I (PMY2201)

Aplicación de consola en Kotlin para una pizzería. Registra al cliente con validación de teléfono y correo, toma el pedido, verifica los ingredientes del inventario (con pasos de reabastecimiento si faltan), asigna al repartidor disponible más cercano, notifica al cliente y genera el resumen del pedido y el reporte de pedidos del día.

## Cómo ejecutarlo

Requiere **JDK 17 o superior**.

- **IntelliJ IDEA:** *File → Open* → seleccionar esta carpeta (se importa como proyecto Gradle) → abrir `src/Main.kt` → ▶ junto a `fun main()`.
- **Terminal:** `gradlew run` (Windows) o `./gradlew run` (macOS/Linux).

## Estructura

| Archivo | Contenido |
|---|---|
| [`src/Main.kt`](src/Main.kt) | Flujo principal, resumen del pedido y reporte del día |
| [`src/Modelos.kt`](src/Modelos.kt) | Clases `Usuario`, `Cliente`, `Repartidor`, `Producto`, `Pedido`, `Inventario` y el enum `EstadoPedido` |
| [`src/Operaciones.kt`](src/Operaciones.kt) | Funciones de cálculo del total, ingredientes por lote, reabastecimiento y asignación de repartidor |
| [`src/Utilidades.kt`](src/Utilidades.kt) | Validación y formato de strings, registro de cliente y lectura segura desde consola |

## Cumplimiento de la pauta

| Criterio | Implementación |
|---|---|
| **Flujos de decisión, arreglos y strings** | `when` en `Inventario.verificarDisponibilidad()` clasifica cada ingrediente como OK, INSUFICIENTE o AGOTADO, y `mostrarPasosReabastecimiento()` detalla cómo reponer. Los pedidos del día se guardan en un `Array<Pedido>`; `imprimirReporteDelDia()` los recorre con `for` y los agrupa en completados y pendientes con `filter`. `validarTelefono()` y `validarCorreo()` validan los datos y, si son incorrectos, usan `(000) 000-0000` y `correo@invalido.com`. `formatearNombre()` normaliza mayúsculas y espacios, y `formatearPesos()` agrega separadores de miles. |
| **Funciones y colecciones** | `calcularTotales()` aplica el 10 % de descuento a pedidos de más de 3 pizzas y el IVA del 19 %. `prepararLote()` suma los ingredientes de las pizzas con `calcularIngredientesLote()`, verifica el stock y lo descuenta. El inventario es un `Map` (ingrediente → cantidad), el menú y los repartidores son `listOf`, y las zonas con cobertura un `setOf`. `buscarRepartidorMasCercano()` ordena por distancia y usa `find`. |
| **Clases y métodos** | `Producto.detallarProducto()`. `Pedido` tiene `calcularTotal()`, `cambiarEstado()`, `asignarRepartidor()` y agrupa los productos repetidos con `groupingBy`. Herencia: `open class Usuario` → `Cliente` y `Repartidor`, con `describir()` sobrescrito (polimorfismo en "Usuarios del sistema") y `Repartidor.entregarPedido()`. Encapsulamiento con `private set` en el estado y el repartidor del pedido, y con el `Map` privado de `Inventario`. |
| **try-catch y operadores seguros** | `leerEntero()` usa `try-catch` para entradas no numéricas, negativas o fuera de rango, y vuelve a preguntar. Un pedido sin productos lanza "Costo de pedido no válido" y se informa sin detener el programa. `Inventario.reponer()` rechaza cantidades negativas y `descontar()` impide que el stock quede negativo. `?.` y `?:` en las notas del pedido ("Sin notas"), el repartidor ("Sin asignar") y los datos ingresados. `let` envía la notificación solo si el cliente tiene un correo válido. |
| **Resumen con plantillas de strings** | `imprimirResumen()` muestra el cliente (nombre, teléfono y correo), los productos (nombre, precio, cantidad y monto), el subtotal, el descuento, el IVA, el total y el estado, usando `"$variable"` y `"${expresión}"`. |
| **Código limpio** | Lógica separada en cuatro archivos por responsabilidad, nombres descriptivos en español y constantes para las tasas y los valores por defecto. |

**Decisiones de diseño**
- **Estado "Entregado":** el enunciado pide agrupar pedidos completados y pendientes, así que se agregó este estado a los tres pedidos (En preparación, Listo, En camino). Lo asigna `Repartidor.entregarPedido()`.
- **Cálculo del total:** el descuento se aplica sobre el subtotal y el IVA sobre el monto con descuento.
- **Pedidos anteriores:** el programa parte con cuatro pedidos ya registrados en el día, para que el reporte tenga datos de todos los estados.

## Pruebas realizadas

Inventario inicial: Masa 20, Salsa de tomate 20, Queso 15, Pepperoni 8, Jamón 4, Piña 4, Champiñones 0, Pimentón 5.

| Caso | Entrada | Resultado |
|---|---|---|
| Pedido con descuento | `ana   PÉREZ`, `+56 9 8765 4321`, `Ana@Mail.com`, zona `sur`, 4 Pepperoni | Nombre "Ana Pérez", correo en minúsculas. Subtotal $35.960, descuento $3.596, IVA $6.149, **total $38.513**. Repartidora Andrea (zona Sur). Notificación enviada. |
| Datos inválidos | Nombre y dirección vacíos, teléfono `12ab`, correo `correo@`, zona `Marte`; opciones `x`, `-1`, `9`; cantidad `0` | Avisos y valores por defecto. Cada número inválido muestra su mensaje y vuelve a preguntar. Sin notificación por correo inválido. Zona Centro: Valentina está en ruta, así que se asigna la disponible más cercana (Andrea, 0,8 km). |
| Ingrediente agotado | 1 Vegetariana; reposición `-5` y luego `4` | Champiñones AGOTADO con pasos de reabastecimiento. `-5` se rechaza. Con 4 unidades, la nueva verificación aprueba el pedido. |
| Ingrediente insuficiente | 5 Pepperoni; reposición `0` | Pepperoni INSUFICIENTE (necesita 10, hay 8). El pedido se rechaza y el inventario no cambia. |
| Pedido vacío | `0` en el primer producto | "Costo de pedido no válido". El programa continúa con el reporte. |
| Varios productos | 4 Hawaiana y 1 Margarita, zona Oriente; `s` al final | Total $49.212, Mario (zona Oriente). Muestra los resúmenes de los 5 pedidos del día. |
| Sin entrada (EOF) | — | No falla: usa valores por defecto y termina con el reporte. |

## Evidencias

Capturas de una ejecución en IntelliJ IDEA (JDK 21):

![Registro del cliente y pedido](evidencias/01_registro_y_pedido.png)
![Resumen del pedido](evidencias/02_resumen_pedido.png)
![Reporte del día](evidencias/03_reporte_del_dia.png)
![Falta de ingredientes](evidencias/04_falta_ingredientes.png)
