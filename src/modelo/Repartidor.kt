package modelo

class Repartidor(
    nombre: String,
    telefono: String,
    correo: String,
    val zona: String,
    val distanciaKm: Double
) : Usuario(nombre, telefono, correo) {

    var disponible: Boolean = true
        private set

    fun tomarPedido(pedido: Pedido) {
        pedido.asignarRepartidor(this)
        disponible = false
    }

    fun entregarPedido(pedido: Pedido) {
        pedido.cambiarEstado(EstadoPedido.ENTREGADO)
        disponible = true
        println("🛵 $nombre entregó el pedido #${pedido.id} a ${pedido.cliente.nombre}. Estado: ${pedido.ultimoCambio()}")
    }

    override fun describir(): String {
        val situacion = if (disponible) "disponible" else "en ruta"
        return "Repartidor · $nombre – zona $zona, a $distanciaKm km, $situacion · ${contacto()}"
    }
}
