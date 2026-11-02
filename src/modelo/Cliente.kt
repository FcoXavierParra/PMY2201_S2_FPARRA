package modelo

import utils.CORREO_POR_DEFECTO

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
