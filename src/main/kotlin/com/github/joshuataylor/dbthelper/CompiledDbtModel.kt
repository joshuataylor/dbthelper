package com.github.joshuataylor.dbthelper

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompiledDbtModel(
    @SerialName("compiled_code") val compiledCode: String,
    @SerialName("compiled_path") val compiledPath: String,
    @SerialName("compiled_full_path") val compiledFullPath: String,
)