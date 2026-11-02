package utils

const val ANCHO = 52

// ---------------------------------------------------------------------
// Entrada por consola
// ---------------------------------------------------------------------
fun leerTexto(mensaje: String): String? {
    print(mensaje)
    return readlnOrNull()?.trim()?.takeIf { it.isNotEmpty() }
}

// Repite la pregunta hasta recibir un entero dentro del rango. Sin más entrada (EOF) devuelve valorPorDefecto
fun leerEntero(mensaje: String, rango: IntRange, valorPorDefecto: Int): Int {
    while (true) {
        print(mensaje)
        val entrada = readlnOrNull() ?: return valorPorDefecto
        try {
            val numero = entrada.trim().toInt()
            if (numero < 0) throw IllegalArgumentException("El valor no puede ser negativo ($numero).")
            if (numero !in rango) throw IllegalArgumentException("Ingrese un valor entre ${rango.first} y ${rango.last}.")
            return numero
        } catch (e: NumberFormatException) {
            println("  ⚠ \"$entrada\" no es un número válido. Intente nuevamente.")
        } catch (e: IllegalArgumentException) {
            println("  ⚠ ${e.message}")
        }
    }
}

// ---------------------------------------------------------------------
// Salida por consola
// ---------------------------------------------------------------------

// 38513 -> "$38.513"
fun formatearPesos(monto: Int): String =
    "$" + monto.toString().reversed().chunked(3).joinToString(".").reversed()

fun filaMonto(etiqueta: String, monto: String): String = "  ${etiqueta.padEnd(38)}${monto.padStart(12)}"

fun titulo(texto: String) {
    println("=".repeat(ANCHO))
    println(texto.padStart((ANCHO + texto.length) / 2))
    println("=".repeat(ANCHO))
}

fun seccion(texto: String) {
    println("\n--- $texto ---")
}

fun mostrarPasosReabastecimiento(faltantes: Map<String, Int>) {
    val detalle = faltantes.entries.joinToString(", ") { (ingrediente, cantidad) -> "$cantidad de $ingrediente" }
    println("✘ Ingredientes insuficientes: ${faltantes.keys.joinToString(", ")}")
    println("  Pasos para reabastecer:")
    println("   1. Solicitar al proveedor al menos: $detalle.")
    println("   2. Recibir la mercadería y revisar cantidad y fecha de vencimiento.")
    println("   3. Registrar el ingreso en el inventario.")
    println("   4. Volver a verificar el pedido.")
}
