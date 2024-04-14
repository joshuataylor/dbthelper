package com.github.joshuataylor.dbthelper.runConfiguration

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

class DbtHelperRunConfigurationOptions : RunConfigurationOptions() {
    /**
     * The name of the model to compile.
     */
    private val myCompileModel: StoredProperty<String?> =
        string("compileModel").provideDelegate(
            this,
            "compileModel",
        )

    /**
     * Environment variables to set before running the query.
     */
    private val myEnvironmentVariables: StoredProperty<MutableMap<String, String>> =
        map<String, String>().provideDelegate(
            this,
            "environmentVariables",
        )

    /**
     * The name of the data source to use for the query.
     */
    private val myDataSourceName: StoredProperty<String?> =
        string("dataSourceName").provideDelegate(
            this,
            "dataSourceName",
        )

    /**
     * How many rows to limit the query to, wraps the entire query in a `SELECT * FROM (...) LIMIT ?`
     */
    private val myQueryLimit: StoredProperty<String?> =
        string("100").provideDelegate(
            this,
            "queryLimit",
        )

    /**
     * Which FROM to limit the query results for.
     */
    private val myQueryLimitFirstFrom: StoredProperty<String?> =
        string("").provideDelegate(
            this,
            "queryLimit",
        )

    /**
     * Snowflake Specific - Sets the Snowflake session parameter `ROWS_PER_RESULTSET`
     * before and unsets the parameter after each query execution.
     */
    private val useRowsPerResultSet: StoredProperty<Boolean> =
        property(false).provideDelegate( // Default value set to true
            this,
            "useRowsPerResultSet",
        )

    private val myDbtDirectory: StoredProperty<String?> =
        string("dbtDirectory").provideDelegate(
            this,
            "dbtDirectory",
        )

    /**
     * The name of the model to compile.
     */
    var compileModel: String?
        get() = myCompileModel.getValue(this)
        set(value) {
            myCompileModel.setValue(this, value)
        }

    var dataSourceName: String?
        get() = myDataSourceName.getValue(this)
        set(value) {
            myDataSourceName.setValue(this, value)
        }

    var environmentVariables: MutableMap<String, String>
        get() = myEnvironmentVariables.getValue(this)
        set(value) {
            myEnvironmentVariables.setValue(this, value)
        }

    // @todo make this an integer
    var queryLimit: String?
        get() = myQueryLimit.getValue(this)
        set(value) {
            myQueryLimit.setValue(this, value)
        }

    var queryLimitFirstFrom: String?
        get() = myQueryLimitFirstFrom.getValue(this)
        set(value) {
            myQueryLimitFirstFrom.setValue(this, value)
        }

    var dbtDirectory: String?
        get() = myDbtDirectory.getValue(this)
        set(value) {
            myDbtDirectory.setValue(this, value)
        }
}
