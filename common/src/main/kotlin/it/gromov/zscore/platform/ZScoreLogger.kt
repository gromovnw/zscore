package it.gromov.zscore.platform

interface ZScoreLogger {
    fun warn(message: String, error: Throwable? = null)
}
