package ru.hogeltbellai.CloudixNetwork;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum Debug {
    MYSQL, GOALS, CORE, SOCKET, PACKET;
    private static final Logger logger;
    private boolean enabled;

    static {
        logger = CNPluginBungee.core().getLogger();
    }

    Debug() {
        this.enabled = false;
    }

    public void info(String message) {
        log(Level.INFO, message);
    }

    public void warning(String message) {
        log(Level.WARNING, message);
    }

    public void severe(String message) {
        log(Level.SEVERE, message);
    }

    public void logException(String message, Throwable throwable) {
        if (this.enabled) {
            logger.log(Level.SEVERE, formatMessage(message), throwable);
        }
    }

    private void log(Level level, String message) {
        if (this.enabled) {
            logger.log(level, formatMessage(message));
        }
    }

    private String formatMessage(String message) {
        return "[" + this.name() + "] > " + message;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public static void setGlobalEnabled(boolean enabled) {
        for (Debug debug : values()) {
            debug.setEnabled(enabled);
        }
    }
}