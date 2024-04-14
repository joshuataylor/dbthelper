package com.github.joshuataylor.dbthelper.runConfiguration

import com.intellij.database.dataSource.LocalDataSourceManager
import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class DbtHelperSettingsEditor(project: Project) : SettingsEditor<DbtHelperRunConfiguration>() {
    private val scriptModelNameComponent = JBTextField()
    private val queryLimitNameComponent = JBTextField()
    private val envVarsComponent = EnvironmentVariablesComponent().apply {
        text = "Environment Variables"
    }
    private val datasourceNameComponent: ComboBox<String> = ComboBox(
        (listOf("") + LocalDataSourceManager.getInstance(project).dataSources.map { it.name }).toTypedArray()
    )
    private val queryLimitFirstFrom = JBTextField()
    private val dbtDirectoryComponent = JBTextField().apply {
        text = "dbt Directory"
    }

    private val myPanel: JPanel = FormBuilder.createFormBuilder()
        .addLabeledComponent("Model Name:", scriptModelNameComponent)
        .addLabeledComponent("Data Source:", datasourceNameComponent)
        .addLabeledComponent("Query Limit:", queryLimitNameComponent)
        .addLabeledComponent("dbt Directory:", dbtDirectoryComponent)
        .addLabeledComponent("Query Limit First From:", queryLimitFirstFrom)
        .addComponent(envVarsComponent)
        .panel

    override fun resetEditorFrom(dbtHelperRunConfiguration: DbtHelperRunConfiguration) {
        scriptModelNameComponent.text = dbtHelperRunConfiguration.compileModel
        envVarsComponent.envData = EnvironmentVariablesData.create(dbtHelperRunConfiguration.environmentVariables, true)
        datasourceNameComponent.selectedItem = dbtHelperRunConfiguration.dataSourceName
        queryLimitNameComponent.text = dbtHelperRunConfiguration.queryLimit
        dbtDirectoryComponent.text = dbtHelperRunConfiguration.dbtDirectory
        queryLimitFirstFrom.text = dbtHelperRunConfiguration.queryLimit
    }

    override fun applyEditorTo(dbtHelperRunConfiguration: DbtHelperRunConfiguration) {
        dbtHelperRunConfiguration.compileModel = scriptModelNameComponent.text
        dbtHelperRunConfiguration.environmentVariables = envVarsComponent.envData.envs
        dbtHelperRunConfiguration.dataSourceName = datasourceNameComponent.selectedItem as? String ?: ""
        dbtHelperRunConfiguration.queryLimit = queryLimitNameComponent.text ?: "100"
        dbtHelperRunConfiguration.dbtDirectory = dbtDirectoryComponent.text
        dbtHelperRunConfiguration.queryLimit = queryLimitFirstFrom.text
    }

    override fun createEditor(): JComponent = myPanel
}
