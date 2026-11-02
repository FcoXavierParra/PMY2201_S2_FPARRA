package modelo

import utils.formatearPesos

// Pizza del menú con sus ingredientes por unidad (ingrediente -> cantidad)
class Producto(val nombre: String, val precio: Int, val ingredientes: Map<String, Int>) {
    init {
        require(precio > 0) { "El precio de $nombre debe ser mayor a 0" }
    }

    fun detallarProducto(): String {
        val listaIngredientes = ingredientes.keys.joinToString(", ")
        return "${nombre.padEnd(12)} ${formatearPesos(precio).padStart(7)}  ($listaIngredientes)"
    }
}
