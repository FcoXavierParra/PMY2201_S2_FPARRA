// =====================================================================
//  Paso 3: Programación orientada a objetos
//  Entidades del sistema: usuarios (herencia), productos, pedidos e
//  inventario, cada una con la lógica que le corresponde.
// =====================================================================

enum class EstadoPedido(val etiqueta: String) {
    EN_PREPARACION("En preparación"),
    LISTO("Listo"),
    EN_CAMINO("En camino"),
    ENTREGADO("Entregado")
}

// ---------------------------------------------------------------------
// Usuarios: clase base abierta y dos especializaciones
// ---------------------------------------------------------------------
open class Usuario(val nombre: String, val telefono: String, val correo: String) {
    fun contacto(): String = "$telefono · $correo"

    open fun describir(): String = "$nombre (${contacto()})"
}

class Cliente(
    nombre: String,
    telefono: String,
    correo: String,
    val direccion: String,
    val zona: String
) : Usuario(nombre, telefono, correo) {

    // Solo los clientes con correo válido reciben notificaciones (null si no tiene)
    val correoNotificaciones: String?
        get() = correo.takeIf { it != CORREO_POR_DEFECTO }

    override fun describir(): String = "Cliente    · $nombre – $direccion (zona $zona) · ${contacto()}"
}

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
        disponible = false
        pedido.asignarRepartidor(this)
    }

    fun entregarPedido(pedido: Pedido) {
        pedido.cambiarEstado(EstadoPedido.ENTREGADO)
        disponible = true
        println("🛵 $nombre entregó el pedido #${pedido.id} a ${pedido.cliente.nombre}.")
    }

    override fun describir(): String {
        val situacion = if (disponible) "disponible" else "en ruta"
        return "Repartidor · $nombre – zona $zona, a $distanciaKm km, $situacion · ${contacto()}"
    }
}

// ---------------------------------------------------------------------
// Producto: pizza del menú con sus ingredientes por unidad
// ---------------------------------------------------------------------
class Producto(val nombre: String, val precio: Int, val ingredientes: Map<String, Int>) {
    init {
        require(precio > 0) { "El precio de $nombre debe ser mayor a 0" }
    }

    fun detallarProducto(): String {
        val listaIngredientes = ingredientes.keys.joinToString(", ")
        return "${nombre.padEnd(12)} ${formatearPesos(precio).padStart(7)}  ($listaIngredientes)"
    }
}

// ---------------------------------------------------------------------
// Pedido: cliente, lista de productos, estado y repartidor
// ---------------------------------------------------------------------
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

    fun calcularTotal(): Totales = calcularTotales(productos.sumOf { it.precio }, productos.size)

    // Agrupa los productos repetidos: Producto -> cantidad pedida
    fun detalleProductos(): Map<Producto, Int> = productos.groupingBy { it }.eachCount()

    fun resumenProductos(): String =
        detalleProductos().entries.joinToString(", ") { (producto, cantidad) -> "${cantidad}x ${producto.nombre}" }

    fun cambiarEstado(nuevoEstado: EstadoPedido) {
        estado = nuevoEstado
    }

    fun asignarRepartidor(asignado: Repartidor) {
        repartidor = asignado
        cambiarEstado(EstadoPedido.EN_CAMINO)
    }

    companion object {
        private var siguienteId = 1001
    }
}

// ---------------------------------------------------------------------
// Inventario: Map ingrediente -> cantidad disponible (encapsulado)
// ---------------------------------------------------------------------
class Inventario(stockInicial: Map<String, Int>) {
    private val stock: MutableMap<String, Int> = stockInicial.toMutableMap()

    fun disponible(ingrediente: String): Int = stock[ingrediente] ?: 0

    // Paso 1: when evalúa cada ingrediente. Devuelve los faltantes (ingrediente -> unidades que faltan)
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
