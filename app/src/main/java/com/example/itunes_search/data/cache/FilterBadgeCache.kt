package com.example.itunes_search.data.cache

import com.example.itunes_search.domain.model.TrackFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Один экземпляр создаётся в AppContainer и передаётся каталогу и настройкам.
// Сам флаг существует только в памяти; после запуска он вычисляется из фильтров.
class FilterBadgeCache(initialFilters: TrackFilters) {
    private val _isVisible = MutableStateFlow(initialFilters.isActive)
    val isVisible = _isVisible.asStateFlow()

    fun update(filters: TrackFilters) {
        _isVisible.value = filters.isActive
    }
}
