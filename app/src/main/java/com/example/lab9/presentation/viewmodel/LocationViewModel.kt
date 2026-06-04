package com.example.lab9.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab9.data.repository.LocationRepository
import com.example.lab9.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class LocationViewModel(
    application: Application,
    private val repository: LocationRepository
) : AndroidViewModel(application) {

    val locations: StateFlow<List<LocationEntity>> = repository.observeCachedLocations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _status = MutableStateFlow<String>("Готово")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchAndSaveCurrentLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            _status.value = "Отримання локації..."

            val location = repository.requestCurrentLocation()
                ?: repository.getLastKnownLocation()

            if (location != null) {
                repository.saveLocationToDb(location)
                _status.value = "Збережено о ${Date(location.time)}"
            } else {
                _status.value = "Не вдалося отримати локацію"
            }

            _isLoading.value = false
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
            _status.value = "Кеш очищено"
        }
    }
}
