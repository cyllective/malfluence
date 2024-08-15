package com.cyllective.malfluence.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
    private static final Logger logger = LoggerFactory.getLogger(Log.class);

    public static void Debug(String message) {
        logger.debug(String.format("[MALFLUENCE] %s", message));
    }

    public static void Error(Exception e) {
        logger.debug(String.format("[MALFLUENCE] ERROR: %s", e.getMessage()));
    }
}
