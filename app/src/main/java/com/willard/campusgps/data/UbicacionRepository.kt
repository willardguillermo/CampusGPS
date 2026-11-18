package com.willard.campusgps.data

import com.willard.campusgps.model.Posicion

interface UbicacionRepository {
    suspend fun obtenerPosicion(): Result<Posicion>
    fun playServicesDisponible(): Boolean
}

class UbicacionNoDisponibleException : Exception("No se pudo obtener la ubicación")