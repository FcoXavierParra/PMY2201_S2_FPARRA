package modelo

enum class EstadoPedido(val etiqueta: String) {
    EN_PREPARACION("En preparación"),
    LISTO("Listo"),
    EN_CAMINO("En camino"),
    ENTREGADO("Entregado");

    // Único estado al que se puede avanzar desde el actual (null = estado final)
    fun siguiente(): EstadoPedido? = when (this) {
        EN_PREPARACION -> LISTO
        LISTO -> EN_CAMINO
        EN_CAMINO -> ENTREGADO
        ENTREGADO -> null
    }
}
