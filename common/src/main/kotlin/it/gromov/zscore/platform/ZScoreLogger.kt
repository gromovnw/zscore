package it.gromov.zscore.platform

interface ZScoreLogger {
    fun info(message: String)
    fun warn(message: String, error: Throwable? = null)
}
