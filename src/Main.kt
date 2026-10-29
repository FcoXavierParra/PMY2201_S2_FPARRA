import kotlin.math.roundToInt

// =====================================================================
//  Aplicando utilidades de Kotlin – Pizzería
//  Actividad formativa S2 · Desarrollo App Móviles I (PMY2201)
//  Flujo: menú → registro de cliente → pedido → verificación de stock →
//  repartidor → notificación → resumen → reporte del día
// =====================================================================

const val ANCHO = 52

fun main() {
    // -----------------------------------------------------------------
    // Datos base: menú (listOf), inventario (Map) y repartidores (listOf)
    // -----------------------------------------------------------------
    val menu: List<Producto> = listOf(
        Producto("Margarita", 7990, mapOf("Masa" to 1, "Salsa de tomate" to 1, "Queso" to 2)),
        Producto("Pepperoni", 8990, mapOf("Masa" to 1, "Salsa de tomate" to 1, "Queso" to 1, "Pepperoni" to 2)),
        Producto("Hawaiana", 9490, mapOf("Masa" to 1, "Salsa de tomate" to 1, "Queso" to 1, "Jamón" to 1, "Piña" to 1)),
        Producto("Vegetariana", 9990, mapOf("Masa" to 1, "Salsa de tomate" to 1, "Queso" to 1, "Champiñones" to 2, "Pimentón" to 1))
    )

    val inventario = Inventario(
        mapOf(
            "Masa" to 20, "Salsa de tomate" to 20, "Queso" to 15, "Pepperoni" to 8,
            "Jamón" to 4, "Piña" to 4, "Champiñones" to 0, "Pimentón" to 5
        )
    )

    val repartidores: List<Repartidor> = listOf(
        Repartidor("Carlos Muñoz", "+56 9 1111 2222", "carlos@pizzeria.cl", "Norte", 1.2),
        Repartidor("Andrea Rojas", "+56 9 3333 4444", "andrea@pizzeria.cl", "Sur", 0.8),
        Repartidor("Valentina Soto", "+56 9 5555 6666", "valentina@pizzeria.cl", "Centro", 0.5),
        Repartidor("Mario Díaz", "+56 9 7777 8888", "mario@pizzeria.cl", "Oriente", 2.5)
    )

    // Pedidos que ya se registraron hoy, antes de este pedido
    val (margarita, pepperoni, hawaiana, vegetariana) = menu
    val pedidosAnteriores: Array<Pedido> = arrayOf(
        Pedido(
            registrarCliente("Juan Pérez", "912345678", "juan.perez@mail.com", "Calle Moneda 1020", "Centro"),
            listOf(margarita, margarita), estadoInicial = EstadoPedido.ENTREGADO
        ),
        Pedido(
            registrarCliente("María López", "+56 9 2233 4455", "maria@mail.com", "Av. Independencia 500", "Norte"),
            listOf(pepperoni, hawaiana), estadoInicial = EstadoPedido.LISTO
        ),
        Pedido(
            registrarCliente("Pedro Silva", "987651234", "pedro.silva@mail.com", "San Diego 340", "Centro"),
            listOf(pepperoni, pepperoni, pepperoni, pepperoni, margarita)
        ).also { repartidores[2].tomarPedido(it) },   // Valentina sale con este pedido
        Pedido(
            registrarCliente("Sofía Vera", "998877665", "sofia.vera@mail.com", "Gran Avenida 4500", "Sur"),
            listOf(vegetariana), notas = "Sin pimentón, por favor"
        )
    )

    titulo("PIZZERÍA LA NAPOLITANA – Nuevo pedido")

    println("MENÚ")
    for ((indice, producto) in menu.withIndex()) {
        println("  ${indice + 1}. ${producto.detallarProducto()}")
    }

    // -----------------------------------------------------------------
    // Registro del cliente (strings: validación y formato)
    // -----------------------------------------------------------------
    seccion("REGISTRO DE CLIENTE")
    val cliente = registrarCliente(
        nombre = leerTexto("Nombre: "),
        telefono = leerTexto("Teléfono (9 dígitos): "),
        correo = leerTexto("Correo electrónico: "),
        direccion = leerTexto("Dirección de entrega: "),
        zona = leerTexto("Zona (${ZONAS_CON_COBERTURA.joinToString("/")}): ")
    )
    println("✔ ${cliente.describir()}")

    // -----------------------------------------------------------------
    // Toma del pedido (while + try-catch en leerEntero)
    // -----------------------------------------------------------------
    seccion("PEDIDO")
    val productosPedidos = mutableListOf<Producto>()
    while (true) {
        val opcion = leerEntero("Número de pizza (0 para terminar): ", 0..menu.size, valorPorDefecto = 0)
        if (opcion == 0) break
        val pizza = menu[opcion - 1]
        val cantidad = leerEntero("Cantidad de ${pizza.nombre}: ", 1..20, valorPorDefecto = 1)
        repeat(cantidad) { productosPedidos.add(pizza) }
        println("  + $cantidad ${pizza.nombre} (llevas ${productosPedidos.size} pizza(s))")
    }
    val notas: String? = leerTexto("Notas del pedido (opcional, Enter para omitir): ")

    // Un pedido sin productos tiene costo 0: calcularTotal() lanza la excepción
    val pedido: Pedido? = try {
        Pedido(cliente, productosPedidos.toList(), notas).also { it.calcularTotal() }
    } catch (e: IllegalArgumentException) {
        println("✘ ${e.message}. El pedido no se registró.")
        null
    }

    // -----------------------------------------------------------------
    // Verificación de ingredientes, reabastecimiento y despacho
    // -----------------------------------------------------------------
    var pedidoConfirmado = false
    if (pedido != null) {
        seccion("VERIFICACIÓN DE INGREDIENTES")
        var faltantes = prepararLote(pedido.productos, inventario)

        if (faltantes.isNotEmpty()) {
            mostrarPasosReabastecimiento(faltantes)
            println("\nRegistrar reposición (0 = no reponer):")
            for (ingrediente in faltantes.keys) {
                val cantidad = leerEntero("  Unidades de $ingrediente: ", 0..100, valorPorDefecto = 0)
                try {
                    inventario.reponer(ingrediente, cantidad)
                } catch (e: IllegalArgumentException) {
                    println("  ⚠ ${e.message}")
                }
            }
            println("\nNueva verificación:")
            faltantes = prepararLote(pedido.productos, inventario)
        }

        if (faltantes.isEmpty()) {
            pedidoConfirmado = true
            println("✔ Ingredientes descontados del inventario.")
            pedido.cambiarEstado(EstadoPedido.LISTO)

            buscarRepartidorMasCercano(repartidores, cliente.zona)?.let { repartidor ->
                repartidor.tomarPedido(pedido)
                println("🛵 Repartidor asignado: ${repartidor.nombre} (zona ${repartidor.zona}, ${repartidor.distanciaKm} km)")
            } ?: println("⚠ No hay repartidores disponibles. El pedido queda listo para despacho.")

            // let: la notificación solo se envía si el cliente tiene un correo válido
            cliente.correoNotificaciones?.let { correo ->
                println("📧 Notificación enviada a $correo: pedido #${pedido.id} ${pedido.estado.etiqueta.lowercase()}.")
            } ?: println("📧 Sin correo válido: no se envía notificación.")

            imprimirResumen(pedido)
        } else {
            println("✘ El pedido #${pedido.id} no puede completarse por falta de ingredientes.")
        }
    }

    // -----------------------------------------------------------------
    // Cierre: entregas en curso, usuarios y reporte del día
    // -----------------------------------------------------------------
    seccion("ENTREGAS EN CURSO")
    val pedidoEnRuta = pedidosAnteriores[2]
    pedidoEnRuta.repartidor?.entregarPedido(pedidoEnRuta)

    val pedidosDelDia: Array<Pedido> =
        if (pedidoConfirmado && pedido != null) pedidosAnteriores + pedido else pedidosAnteriores

    // Polimorfismo: cada Usuario se describe según su tipo
    seccion("USUARIOS DEL SISTEMA")
    val usuarios: List<Usuario> = pedidosDelDia.map { it.cliente } + repartidores
    for (usuario in usuarios) println("  ${usuario.describir()}")

    imprimirReporteDelDia(pedidosDelDia)

    seccion("INVENTARIO FINAL")
    println("  ${inventario.resumen()}")

    if (pedidoConfirmado && pedido != null) {
        val verTodos = leerTexto("\n¿Ver el resumen detallado de todos los pedidos del día? (s/N): ")
        if (verTodos.equals("s", ignoreCase = true)) {
            for (otroPedido in pedidosDelDia) imprimirResumen(otroPedido)
        }
    }
    println("\nFin de la jornada. ¡Gracias por usar el sistema!")
}

// ---------------------------------------------------------------------
// Paso 5: resumen de un pedido con plantillas de strings
// ---------------------------------------------------------------------
fun imprimirResumen(pedido: Pedido) {
    val totales = pedido.calcularTotal()
    val cliente = pedido.cliente

    println()
    titulo("CONFIRMACIÓN DEL PEDIDO #${pedido.id}")
    println("CLIENTE")
    println("  Nombre:     ${cliente.nombre}")
    println("  Teléfono:   ${cliente.telefono}")
    println("  Correo:     ${cliente.correo}")
    println("  Dirección:  ${cliente.direccion} (zona ${cliente.zona})")
    println("-".repeat(ANCHO))
    println("${"PRODUCTOS".padEnd(24)}${"Precio".padStart(10)}${"Cant.".padStart(7)}${"Monto".padStart(11)}")
    for ((producto, cantidad) in pedido.detalleProductos()) {
        println("  ${producto.nombre.padEnd(22)}${formatearPesos(producto.precio).padStart(10)}${cantidad.toString().padStart(7)}${formatearPesos(producto.precio * cantidad).padStart(11)}")
    }
    println("-".repeat(ANCHO))
    val etiquetaDescuento = if (totales.descuento > 0) {
        "Descuento ${(TASA_DESCUENTO * 100).roundToInt()}% (más de $MINIMO_PIZZAS_DESCUENTO pizzas):"
    } else {
        "Descuento: no aplica"
    }
    println(filaMonto("Subtotal:", formatearPesos(totales.subtotal)))
    println(filaMonto(etiquetaDescuento, if (totales.descuento > 0) "-${formatearPesos(totales.descuento)}" else formatearPesos(0)))
    println(filaMonto("IVA (${(TASA_IVA * 100).roundToInt()}%):", formatearPesos(totales.impuesto)))
    println(filaMonto("TOTAL:", formatearPesos(totales.total)))
    println("-".repeat(ANCHO))
    println("  Estado:     ${pedido.estado.etiqueta}")
    println("  Repartidor: ${pedido.repartidor?.let { "${it.nombre} (zona ${it.zona})" } ?: "Sin asignar"}")
    println("  Notas:      ${pedido.notas ?: "Sin notas"}")
    println("=".repeat(ANCHO))
}

// ---------------------------------------------------------------------
// Paso 1: reporte del día recorriendo el arreglo con for y agrupando con filter
// ---------------------------------------------------------------------
fun imprimirReporteDelDia(pedidos: Array<Pedido>) {
    seccion("REPORTE DE PEDIDOS DEL DÍA")
    println("${"#".padEnd(6)}${"Cliente".padEnd(20)}${"Pizzas".padEnd(30)}${"Total".padStart(9)}  Estado")
    for (pedido in pedidos) {
        val total = formatearPesos(pedido.calcularTotal().total)
        println("${pedido.id}  ${pedido.cliente.nombre.take(18).padEnd(18)}  ${pedido.resumenProductos().padEnd(28)}  ${total.padStart(9)}  ${pedido.estado.etiqueta}")
    }

    val completados = pedidos.filter { it.estado == EstadoPedido.ENTREGADO }
    val pendientes = pedidos.filter { it.estado != EstadoPedido.ENTREGADO }
    println()
    println("Completados (${completados.size}): ${completados.joinToString { "#${it.id}" }}")
    println("Pendientes  (${pendientes.size}): ${pendientes.joinToString { "#${it.id} ${it.estado.etiqueta}" }}")
    println("Venta del día: ${formatearPesos(pedidos.sumOf { it.calcularTotal().total })}")
}

fun filaMonto(etiqueta: String, monto: String): String = "  ${etiqueta.padEnd(38)}${monto.padStart(12)}"

fun titulo(texto: String) {
    println("=".repeat(ANCHO))
    println(texto.padStart((ANCHO + texto.length) / 2))
    println("=".repeat(ANCHO))
}

fun seccion(texto: String) {
    println("\n--- $texto ---")
}
