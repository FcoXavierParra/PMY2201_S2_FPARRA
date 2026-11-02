package modelo

// Map ingrediente -> cantidad disponible, encapsulado en la clase
class Inventario(stockInicial: Map<String, Int>) {
    private val stock: MutableMap<String, Int> = stockInicial.toMutableMap()

    fun disponible(ingrediente: String): Int = stock[ingrediente] ?: 0

    // when evalúa cada ingrediente. Devuelve los faltantes (ingrediente -> unidades que faltan)
    fun verificarDisponibilidad(necesarios: Map<String, Int>): Map<String, Int> {
        val faltantes = mutableMapOf<String, Int>()
        for ((ingrediente, cantidadNecesaria) in necesarios) {
            val disponible = disponible(ingrediente)
            val resultado = when {
                disponible >= cantidadNecesaria -> "OK"
                disponible == 0 -> "AGOTADO"
                else -> "INSUFICIENTE"
            }
            if (resultado != "OK") faltantes[ingrediente] = cantidadNecesaria - disponible
            println("  ${ingrediente.padEnd(16)} necesita ${cantidadNecesaria.toString().padStart(2)} · hay ${disponible.toString().padStart(2)} → $resultado")
        }
        return faltantes
    }

    fun descontar(necesarios: Map<String, Int>) {
        for ((ingrediente, cantidad) in necesarios) {
            val restante = disponible(ingrediente) - cantidad
            if (restante < 0) throw IllegalStateException("El inventario de $ingrediente quedaría negativo ($restante)")
            stock[ingrediente] = restante
        }
    }

    fun reponer(ingrediente: String, cantidad: Int) {
        if (cantidad < 0) throw IllegalArgumentException("La cantidad a reponer no puede ser negativa ($cantidad)")
        stock[ingrediente] = disponible(ingrediente) + cantidad
    }

    fun resumen(): String = stock.entries.joinToString(" · ") { (ingrediente, cantidad) -> "$ingrediente: $cantidad" }
}
