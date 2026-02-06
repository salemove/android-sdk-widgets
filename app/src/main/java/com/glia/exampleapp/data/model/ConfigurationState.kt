package com.glia.exampleapp.data.model

sealed class ConfigurationState {
    data object Idle : ConfigurationState()
    data object Loading : ConfigurationState()
    data object Configured : ConfigurationState()
    data class Error(val message: String) : ConfigurationState()
}
