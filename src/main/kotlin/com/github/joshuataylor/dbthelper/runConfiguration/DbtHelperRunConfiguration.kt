package com.github.joshuataylor.dbthelper.runConfiguration

import com.github.joshuataylor.dbthelper.CompiledDbtModel
import com.github.joshuataylor.dbthelper.DbtHelperQueryExecutor
import com.intellij.database.util.DbImplUtil
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.refreshAndFindVirtualFile
import com.intellij.testFramework.LightVirtualFile
import kotlinx.serialization.json.Json
import java.nio.file.Paths

class DbtHelperRunConfiguration(
    project: Project?,
    factory: ConfigurationFactory?,
    name: String?,
) :
    RunConfigurationBase<DbtHelperRunConfigurationOptions?>(project!!, factory, name) {
    override fun getOptions(): DbtHelperRunConfigurationOptions {
        return super.getOptions() as DbtHelperRunConfigurationOptions
    }

    var compileModel: String?
        get() = options.compileModel
        set(compileModel) {
            options.compileModel = compileModel.toString()
        }

    var environmentVariables: Map<String, String>
        get() = options.environmentVariables
        set(value) {
            options.environmentVariables = value.toMutableMap()
        }

    var dataSourceName: String?
        get() = options.dataSourceName
        set(compileModel) {
            options.dataSourceName = compileModel.toString()
        }

    // @todo make this an integer
    var queryLimit: String?
        get() = options.queryLimit
        set(value) {
            options.queryLimit = value.toString()
        }

    var queryLimitFirstFrom: String?
        get() = options.queryLimitFirstFrom
        set(value) {
            options.queryLimitFirstFrom = value.toString()
        }

    var useRowsPerResultSet: String?
        get() = options.queryLimitFirstFrom
        set(value) {
            options.queryLimitFirstFrom = value.toString()
        }


    var dbtDirectory: String?
        get() = options.dbtDirectory
        set(value) {
            options.dbtDirectory = value.toString()
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration?> {
        return DbtHelperSettingsEditor(this.project)
    }

    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment,
    ): RunProfileState {
        return object : CommandLineState(environment) {
            override fun startProcess(): ProcessHandler {
                val editor =
                    FileEditorManager.getInstance(project).selectedTextEditor
                        ?: throw Exception("No editor found")

                val currentDocument = editor.document

                val currentFile =
                    FileDocumentManager.getInstance().getFile(
                        currentDocument,
                    )

                val compiledFilePath = currentFile!!.path
                if (!compiledFilePath.contains("/models/")) {
                    throw Exception("File must be in the /models/ directory")
                }

                val modelName = currentFile.nameWithoutExtension

                // I want to pass dbtDirectory and modelName as args
                val commandLine =
                    GeneralCommandLine("$dbtDirectory/dbtcompile.sh")
                        .withParameters(dbtDirectory!!, modelName) // assuming '-s' is a valid option for your script
                        .withEnvironment(environmentVariables)

                val processHandler =
                    ProcessHandlerFactory.getInstance()
                        .createColoredProcessHandler(commandLine)

                val outputBuilder = StringBuilder()

                // Attach a process listener to handle process events
                processHandler.addProcessListener(
                    object : ProcessAdapter() {
                        override fun onTextAvailable(
                            event: ProcessEvent,
                            outputType: Key<*>,
                        ) {
                            super.onTextAvailable(event, outputType)
                            // Capture the output
                            if (outputType == ProcessOutputTypes.STDOUT || outputType == ProcessOutputTypes.STDERR) {
                                outputBuilder.append(event.text)
                            }
                        }

                        override fun processTerminated(event: ProcessEvent) {
                            super.processTerminated(event)
                            thisLogger().info("process terminated")

                            // Handle process termination
                            val exitCode = event.exitCode
                            if (exitCode != 0) {
                                throw Exception("Process failed with exit code $exitCode")
                            }
                            if (dataSourceName == null || dataSourceName == "") {
                                throw Exception("Data source name not set")
                            }

                            // find line starting with {
                            val compiledJson = outputBuilder.toString().lines().find { it.startsWith("{") }
                            // does targetPath exist?
                            if (compiledJson == null) {
                                throw Exception("Target path not found")
                            }

                            val json =
                                Json {
                                    useAlternativeNames = true
//                            explicitNulls = false
                                    encodeDefaults = true
                                    ignoreUnknownKeys = true
                                    coerceInputValues = true
                                }

                            val compiledDbtModel = json.decodeFromString<CompiledDbtModel>(compiledJson)

                            // As we have the compiled SQL, we need to now add limit around it
                            // @todo put the limit at the top of the file, so the first ref is the limit
                            var newSQL = compiledDbtModel.compiledCode
                            // If we have a queryLimitFirstFrom, we need to add a limit to the first from
                            // this is hacky as shit right nnow
                            if (queryLimitFirstFrom != null) {
                                // find the first from which is a
                            }

                            if (queryLimit != null) {
                                newSQL = "select * from ($newSQL) limit $queryLimit"
                            }

                            Paths.get(compiledDbtModel.compiledFullPath).refreshAndFindVirtualFile()
                            val compiledFile =
                                VirtualFileManager.getInstance()
                                    .refreshAndFindFileByNioPath(Paths.get(compiledDbtModel.compiledFullPath))
                                    ?: throw Exception("Compiled file not found")

                            val lvf = LightVirtualFile(compiledFile, newSQL, compiledFile.modificationStamp)

                            val dataSource =
                                DbtHelperQueryExecutor.getDataSource(project, dataSourceName!!)
                                    ?: throw Exception("Data source not found")

                            // Ensure that the datasource is configured correctly (drivers, etc)
                            dataSource.ensureDriverConfigured()
                            val driverFiles = DbImplUtil.hasDriverFiles(dataSource)
                            if (!driverFiles) {
                                throw Exception("Data source not configured correctly")
                            }

                            ApplicationManager.getApplication().invokeLater {
                                val dbtHelperQueryExecutor = DbtHelperQueryExecutor(project, dataSource)

                                dbtHelperQueryExecutor.executeQuery(lvf, editor)
                            }
                        }
                    },
                )
                processHandler.startNotify()

                return processHandler
            }
        }
    }
}
