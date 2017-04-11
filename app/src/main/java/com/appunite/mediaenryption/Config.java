package com.appunite.mediaenryption;



public class Config {

    private LogLevel logLevel;

    public enum LogLevel {
        ERRORS,
        DEBUG,
        NONE
    }

    public Config() {
        this.logLevel = LogLevel.NONE;
    }

    public Config(final LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public boolean isErrorLoggingEnabled() {
        return logLevel == LogLevel.ERRORS;
    }

    public boolean isDebugLoggingEnabled() {
        return logLevel == LogLevel.DEBUG;
    }

}
