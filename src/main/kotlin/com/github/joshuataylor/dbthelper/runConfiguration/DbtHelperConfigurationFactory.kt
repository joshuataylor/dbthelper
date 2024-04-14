package com.github.joshuataylor.dbthelper.runConfiguration

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project

class DbtHelperConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun getId(): String = DbtHelperRunConfigurationType.ID

    override fun createTemplateConfiguration(project: Project): RunConfiguration = DbtHelperRunConfiguration(project, this, "DbtHelper")

    override fun getOptionsClass(): Class<out BaseState?> = DbtHelperRunConfigurationOptions::class.java
}
