package com.github.joshuataylor.dbthelper

import com.intellij.database.console.JdbcConsole
import com.intellij.database.console.JdbcConsoleProvider
import com.intellij.database.console.session.DatabaseSession
import com.intellij.database.console.session.DatabaseSessionManager
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.LocalDataSourceManager
import com.intellij.database.run.ConsoleDataRequest
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * Handles the execution of queries in an IntelliJ platform plugin.
 *
 * @property project the current project in which the query is executed
 * @property dataSource the data source for executing the query
 */
class DbtHelperQueryExecutor(private val project: Project, private var dataSource: LocalDataSource) {
    companion object {
        /**
         * Retrieves a data source by name for the given project.
         *
         * @param project the project from which to retrieve the data source
         * @param dataSourceName The name of the datasource to use.
         * @return the matching data source, or null if not found
         */
        fun getDataSource(project: Project, dataSourceName: String): LocalDataSource? =
            LocalDataSourceManager.getInstance(project).dataSources.find { it.name == dataSourceName }
    }

    /**
     * Executes a query based on a given virtual file and editor.
     *
     * @param virtualFile the virtual file associated with the query
     * @param editor the editor from which the query execution is triggered
     */
    fun executeQuery(virtualFile: VirtualFile, editor: Editor) {
        val sessionName = virtualFile.name
        val databaseSession = getOrOpenDatabaseSession(project, dataSource, sessionName)

        val existingJdbcConsole =
            databaseSession.clientsWithFile.firstOrNull { it.virtualFile.name == virtualFile.name } as? JdbcConsole

        // Assign or create a console based on the type of console2
        val console = existingJdbcConsole ?: JdbcConsoleProvider.getValidConsole(project, virtualFile)
        ?: JdbcConsoleProvider.attachConsole(project, databaseSession, virtualFile)
        ?: throw Exception("Console could not be initialized")

        val consoleDataRequest = ConsoleDataRequest.newConsoleRequest(console, editor, console.scriptModel, false)
            ?: throw Exception("ConsoleDataRequest could not be created")

        console.getMessageBus().dataProducer.processRequest(consoleDataRequest)
    }

    /**
     * Retrieves or opens a database session for the given project, data source, and session name.
     *
     * @param project the project for the database session
     * @param dataSource the data source for the database session
     * @param sessionName the name of the session to retrieve or open
     * @return an existing or a newly opened database session
     */
    private fun getOrOpenDatabaseSession(
        project: Project,
        dataSource: LocalDataSource,
        sessionName: String
    ): DatabaseSession =
        DatabaseSessionManager.getSessions(project).find { it.title == sessionName && it.project == project }
            ?: DatabaseSessionManager.openSession(project, dataSource, sessionName)
}
