package utils

import modelo.Inventario
import modelo.Producto
import modelo.Repartidor
import kotlin.math.roundToInt

const val TASA_IVA = 0.19
const val TASA_DESCUENTO = 0.10
const val MINIMO_PIZZAS_DESCUENTO = 3   // el descuento aplica a pedidos de MÁS de 3 pizzas

data class Totales(val subtotal: Int, val descuento: Int, val impuesto: Int, val total: Int)

// Total del pedido: subtotal - descuento (10 % sobre 3 pizzas) + IVA sobre el monto con descuento
fun calcularTotales(subtotal: Int, cantidadPizzas: Int): Totales {
    if (subtotal <= 0) throw IllegalArgumentException("Costo de pedido no válido ($subtotal): el pedido no tiene productos")
    val descuento = if (cantidadPizzas > MINIMO_PIZZAS_DESCUENTO) (subtotal * TASA_DESCUENTO).roundToInt() else 0
    val impuesto = ((subtotal - descuento) * TASA_IVA).roundToInt()
    return Totales(subtotal, descuento, impuesto, subtotal - descuento + impuesto)
}

// Suma los ingredientes de todas las pizzas del lote: ingrediente -> cantidad total necesaria
fun calcularIngredientesLote(productos: List<Producto>): Map<String, Int> {
    val necesarios = mutableMapOf<String, Int>()
    for (producto in productos) {
        for ((ingrediente, cantidad) in producto.ingredientes) {
            necesarios[ingrediente] = (necesarios[ingrediente] ?: 0) + cantidad
        }
    }
    return necesarios
}

// Verifica el stock y, si alcanza, lo descuenta. Devuelve los faltantes (vacío = lote preparado)
fun prepararLote(productos: List<Producto>, inventario: Inventario): Map<String, Int> {
    val necesarios = calcularIngredientesLote(productos)
    val faltantes = inventario.verificarDisponibilidad(necesarios)
    if (faltantes.isEmpty()) inventario.descontar(necesarios)
    return faltantes
}

// Recorre los repartidores del más cercano al más lejano: primero uno disponible de la misma zona,
// si no hay, el disponible más cercano
fun buscarRepartidorMasCercano(repartidores: List<Repartidor>, zona: String): Repartidor? {
    val porCercania = repartidores.sortedBy { it.distanciaKm }
    return porCercania.find { it.disponible && it.zona == zona } ?: porCercania.find { it.disponible }
}
