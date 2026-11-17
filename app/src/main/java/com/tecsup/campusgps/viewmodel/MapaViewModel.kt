package com.tecsup.campusgps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.campusgps.data.LugaresRepository
import com.tecsup.campusgps.data.UbicacionNoDisponibleException
import com.tecsup.campusgps.data.UbicacionRepository
import com.tecsup.campusgps.model.Lugar
import com.tecsup.campusgps.model.LugarConDistancia
import com.tecsup.campusgps.model.Posicion
import com.tecsup.campusgps.model.TipoError
import com.tecsup.campusgps.model.UbicacionUi
import com.tecsup.campusgps.util.distanciaMetros
import com.tecsup.campusgps.util.formatearDistancia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapaViewModel(
    private val ubicacionRepo: UbicacionRepository,
    private val lugaresRepo: LugaresRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UbicacionUi>(UbicacionUi.Inicial)
    val estado: StateFlow<UbicacionUi> = _estado.asStateFlow()

    private val _todosLosLugares = MutableStateFlow(
        lugaresRepo.obtenerLugares().map { LugarConDistancia(it, null) }
    )

    private val _categoriaSeleccionada = MutableStateFlow<String?>(null)
    val categoriaSeleccionada: StateFlow<String?> = _categoriaSeleccionada.asStateFlow()

    private val _radioSeleccionado = MutableStateFlow<Int?>(null)
    val radioSeleccionado: StateFlow<Int?> = _radioSeleccionado.asStateFlow()

    val radiosDisponibles: List<Int> = listOf(100, 200, 500)

    val categorias: List<String> = lugaresRepo.obtenerLugares().map { it.categoria }.distinct()

    val lugares: StateFlow<List<LugarConDistancia>> = combine(
        _todosLosLugares,
        _categoriaSeleccionada
    ) { lista, categoria ->
        if (categoria == null) {
            lista
        } else {
            lista.filter { it.lugar.categoria.equals(categoria, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _todosLosLugares.value)

    val lugaresEnRadio: StateFlow<Set<Int>> = combine(
        _todosLosLugares,
        _radioSeleccionado
    ) { lista, radio ->
        if (radio == null) {
            emptySet()
        } else {
            lista.filter { item -> item.metros != null && item.metros <= radio }
                .map { it.lugar.id }
                .toSet()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _seleccionado = MutableStateFlow<Int?>(null)
    val seleccionado: StateFlow<Int?> = _seleccionado.asStateFlow()

    val masCercano: StateFlow<Lugar?> = lugares
        .map { lista -> lista.firstOrNull { it.metros != null }?.lugar }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun buscar() {
        if (!ubicacionRepo.playServicesDisponible()) {
            _estado.value = UbicacionUi.Error(
                "Este dispositivo no tiene Google Play Services",
                TipoError.SIN_PLAY_SERVICES
            )
            return
        }
        _estado.value = UbicacionUi.Buscando
        viewModelScope.launch {
            ubicacionRepo.obtenerPosicion()
                .onSuccess {
                    _estado.value = UbicacionUi.Lista(it)
                    actualizarDistancias(it)
                }
                .onFailure { _estado.value = errorDe(it) }
        }
    }

    fun permisoDenegado() {
        _estado.value = UbicacionUi.Error(
            "Sin permiso de ubicación no puedo mostrar dónde estás",
            TipoError.SIN_PERMISO
        )
    }

    fun seleccionar(id: Int?) {
        _seleccionado.value = id
    }

    fun seleccionarCategoria(categoria: String?) {
        _categoriaSeleccionada.value = if (_categoriaSeleccionada.value == categoria) null else categoria
    }

    fun seleccionarRadio(metros: Int) {
        _radioSeleccionado.value = if (_radioSeleccionado.value == metros) null else metros
    }

    private fun actualizarDistancias(pos: Posicion) {
        val ordenados = lugaresRepo.obtenerLugares()
            .map { LugarConDistancia(it, distanciaMetros(pos, it)) }
            .sortedBy { it.metros }
        _todosLosLugares.value = ordenados
        Log.d("CampusGPS", ordenados.joinToString { "${it.lugar.nombre}: ${it.metros?.let(::formatearDistancia)}" })
    }

    private fun errorDe(e: Throwable): UbicacionUi.Error = when (e) {
        is SecurityException -> UbicacionUi.Error(
            "Falta el permiso de ubicación",
            TipoError.SIN_PERMISO
        )
        is UbicacionNoDisponibleException -> UbicacionUi.Error(
            "No se pudo obtener la ubicación. Activa el GPS e intenta de nuevo",
            TipoError.GPS_APAGADO
        )
        else -> UbicacionUi.Error(
            e.message ?: "No se pudo obtener la ubicación",
            TipoError.OTRO
        )
    }
}