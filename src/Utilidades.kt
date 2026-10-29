// =====================================================================
//  Paso 1 (strings) y Paso 4 (errores y nulls): validación, formato y
//  lectura segura desde consola
// =====================================================================

const val TELEFONO_POR_DEFECTO = "(000) 000-0000"
const val CORREO_POR_DEFECTO = "correo@invalido.com"
const val ZONA_POR_DEFECTO = "Centro"

val ZONAS_CON_COBERTURA: Set<String> = setOf("Norte", "Sur", "Centro", "Oriente", "Poniente")

private val PATRON_CORREO = Regex("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[a-z]{2,}$")

// Acepta 9 dígitos con o sin +56, espacios, guiones o paréntesis. Formato de salida: +56 9 1234 5678
fun validarTelefono(telefono: String?): String {
    val limpio = telefono?.replace(Regex("[\\s()+-]"), "") ?: ""
    val local = if (limpio.length == 11 && limpio.startsWith("56")) limpio.substring(2) else limpio
    return if (local.length == 9 && local.all { it.isDigit() }) {
        "+56 ${local[0]} ${local.substring(1, 5)} ${local.substring(5)}"
    } else {
        TELEFONO_POR_DEFECTO
    }
}

fun validarCorreo(correo: String?): String {
    val normalizado = correo?.trim()?.lowercase() ?: ""
    return if (PATRON_CORREO.matches(normalizado)) normalizado else CORREO_POR_DEFECTO
}

// "  ana   PÉREZ " -> "Ana Pérez"
fun formatearNombre(nombre: String?): String =
    nombre?.trim()
        ?.split(Regex("\\s+"))
        ?.joinToString(" ") { palabra -> palabra.lowercase().replaceFirstChar { it.uppercase() } }
        ?.ifEmpty { null }
        ?: "Cliente sin nombre"

fun validarZona(zona: String?): String =
    ZONAS_CON_COBERTURA.find { it.equals(zona?.trim(), ignoreCase = true) } ?: ZONA_POR_DEFECTO

fun registrarCliente(nombre: String?, telefono: String?, correo: String?, direccion: String?, zona: String?): Cliente {
    val telefonoValidado = validarTelefono(telefono)
    val correoValidado = validarCorreo(correo)
    val zonaValidada = validarZona(zona)

    if (telefonoValidado == TELEFONO_POR_DEFECTO) println("  ⚠ Teléfono \"${telefono ?: ""}\" inválido. Se usa $TELEFONO_POR_DEFECTO")
    if (correoValidado == CORREO_POR_DEFECTO) println("  ⚠ Correo \"${correo ?: ""}\" inválido. Se usa $CORREO_POR_DEFECTO")
    if (!zonaValidada.equals(zona?.trim(), ignoreCase = true)) println("  ⚠ Zona \"${zona ?: ""}\" sin cobertura. Se asigna $ZONA_POR_DEFECTO")

    return Cliente(
        nombre = formatearNombre(nombre),
        telefono = telefonoValidado,
        correo = correoValidado,
        direccion = direccion?.trim()?.ifEmpty { null } ?: "Sin dirección",
        zona = zonaValidada
    )
}

// 38513 -> "$38.513"
fun formatearPesos(monto: Int): String =
    "$" + monto.toString().reversed().chunked(3).joinToString(".").reversed()

// ---------------------------------------------------------------------
// Entrada por consola
// ---------------------------------------------------------------------
fun leerTexto(mensaje: String): String? {
    print(mensaje)
    return readLine()?.trim()?.takeIf { it.isNotEmpty() }
}

// Repite la pregunta hasta recibir un entero dentro del rango. Sin más entrada (EOF) devuelve valorPorDefecto
fun leerEntero(mensaje: String, rango: IntRange, valorPorDefecto: Int): Int {
    while (true) {
        print(mensaje)
        val entrada = readLine() ?: return valorPorDefecto
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
