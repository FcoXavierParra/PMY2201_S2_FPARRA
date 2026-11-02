package modelo

// Clase base de Cliente y Repartidor (herencia y polimorfismo)
open class Usuario(val nombre: String, val telefono: String, val correo: String) {
    fun contacto(): String = "$telefono · $correo"

    open fun describir(): String = "$nombre (${contacto()})"
}
