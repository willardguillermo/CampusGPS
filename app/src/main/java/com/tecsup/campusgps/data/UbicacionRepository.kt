package com.tecsup.campusgps.data

import com.tecsup.campusgps.model.Posicion

interface UbicacionRepository {
    suspend fun obtenerPosicion(): Result<Posicion>
    fun playServicesDisponible(): Boolean
}

class UbicacionNoDisponibleException : Exception("No se pudo obtener la ubicación")