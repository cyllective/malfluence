package com.cyllective.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class log {
    private static final Logger logger = LoggerFactory.getLogger(log.class);

    public static void Debug(String message) {
        logger.debug(String.format("[DOMPROXY] %s", message));
    }

    public static void Error(Exception e) {
        logger.debug(String.format("[DOMPROXY] ERROR: %s", e.getMessage()));
    }
}