package com.willard.campusgps.data

import kotlinx.coroutines.flow.Flow

interface PreferenciasRepository {
    val ultimoLugarId: Flow<Int?>
    suspend fun guardarUltimoLugar(id: Int)
}
