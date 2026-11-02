package modelo

import utils.Totales
import utils.calcularTotales

class Pedido(
    val cliente: Cliente,
    val productos: List<Producto>,
    val notas: String? = null,
    estadoInicial: EstadoPedido = EstadoPedido.EN_PREPARACION
) {
    val id: Int = siguienteId++

    var estado: EstadoPedido = estadoInicial
        private set

    var repartidor: Repartidor? = null
        private set

    private val historialEstados: MutableList<EstadoPedido> = mutableListOf(estadoInicial)

    fun calcularTotal(): Totales = calcularTotales(productos.sumOf { it.precio }, productos.size)

    // Agrupa los productos repetidos: Producto -> cantidad pedida
    fun detalleProductos(): Map<Producto, Int> = productos.groupingBy { it }.eachCount()

    fun resumenProductos(): String =
        detalleProductos().entries.joinToString(", ") { (producto, cantidad) -> "${cantidad}x ${producto.nombre}" }

    // Solo se avanza al estado siguiente: En preparación → Listo → En camino → Entregado
    fun cambiarEstado(nuevoEstado: EstadoPedido) {
        if (nuevoEstado != estado.siguiente()) {
            throw IllegalStateException("Pedido #$id: no se puede pasar de ${estado.etiqueta} a ${nuevoEstado.etiqueta}")
        }
        estado = nuevoEstado
        historialEstados.add(nuevoEstado)
    }

    fun asignarRepartidor(asignado: Repartidor) {
        cambiarEstado(EstadoPedido.EN_CAMINO)
        repartidor = asignado
    }

    fun ultimoCambio(): String = historialEstados.takeLast(2).joinToString(" → ") { it.etiqueta }

    fun seguimiento(): String = historialEstados.joinToString(" → ") { it.etiqueta }

    companion object {
        private var siguienteId = 1001
    }
}
