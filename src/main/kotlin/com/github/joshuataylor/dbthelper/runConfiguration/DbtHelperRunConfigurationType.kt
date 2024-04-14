package com.github.joshuataylor.dbthelper.runConfiguration

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.NotNullLazyValue

internal class DbtHelperRunConfigurationType : ConfigurationTypeBase(
    ID,
    "DbtHelper",
    "DbtHelper run configuration type",
    NotNullLazyValue.createValue { AllIcons.Nodes.Console },
) {
    init {
        addFactory(DbtHelperConfigurationFactory(this))
    }

    companion object {
        const val ID: String = "DbtHelperRunConfiguration"
    }
}
