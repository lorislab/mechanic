/*
 * Copyright 2017 lorislab.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lorislab.mechanic.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * The console logger.
 *
 * @author Andrej Petras
 */
public class Console {

    /**
     * The console logger.
     */
    private static final Logger CONSOLE_LOGGER = Logger.getLogger("CONSOLE");
    /**
     * The console handler.
     */
    private static final ConsoleHandler CONSOLE_HANDLER = new ConsoleHandler();

    /**
     * The static constructor.
     */
    static {
        CONSOLE_LOGGER.setUseParentHandlers(false);
        CONSOLE_HANDLER.setLevel(Level.ALL);
        CONSOLE_HANDLER.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                StringBuilder sb = new StringBuilder();

                if (record.getLevel() == Level.INFO) {
                }else if (record.getLevel() == Level.SEVERE) {
                    sb.append("[ERROR] !!! ");
                } else {
                    if (record.getLevel().intValue() <= Level.FINE.intValue()) {
                        sb.append("[DEBUG] ");
                    } else {
                        sb.append('[');
                        sb.append(record.getLevel());
                        sb.append("] ");
                    }
                }

                sb.append(formatMessage(record));
                sb.append('\n');
                if (record.getLevel() == Level.SEVERE && record.getThrown() != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw, true);
                    record.getThrown().printStackTrace(pw);
                    sb.append(sw.getBuffer().toString());
                }
                return sb.toString();
            }
        });
        CONSOLE_LOGGER.addHandler(CONSOLE_HANDLER);
        CONSOLE_LOGGER.setLevel(Level.ALL);
    }

    public static Handler getHandler() {
        return CONSOLE_HANDLER;
    }

    /**
     * Writes the info message to the console.
     *
     * @param message the message.
     */
    public static void info(String message) {
        CONSOLE_LOGGER.info(message);
    }

    /**
     * Writes the debug message to the console.
     *
     * @param message the message.
     */
    public static void debug(String message) {
        CONSOLE_LOGGER.fine(message);
    }

    /**
     * Writes the warning message to the console.
     *
     * @param message the message.
     */
    public static void warn(String message) {
        CONSOLE_LOGGER.warning(message);
    }

    /**
     * Writes the warning message to the console.
     *
     * @param message the message.
     * @param objects the list of message parameters.
     */
    public static void warn(String message, Object... objects) {
        CONSOLE_LOGGER.log(Level.WARNING, message, objects);
    }

    /**
     * Writes the error message to the console.
     *
     * @param message the message.
     * @param objects the list of message parameters.
     */
    public static void error(String message, Object... objects) {
        CONSOLE_LOGGER.log(Level.SEVERE, message, objects);
    }

    /**
     * Writes the error message to the console.
     *
     * @param message the message.
     * @param thrown the exception.
     */
    public static void error(String message, Throwable thrown) {
        CONSOLE_LOGGER.log(Level.SEVERE, message, thrown);
    }

    /**
     * Logs the message with the objects.
     *
     * @param message the message.
     * @param objects the list of objects.
     */
    public static void info(String message, Object... objects) {
        CONSOLE_LOGGER.log(Level.INFO, message, objects);
    }

    /**
     * Empty log message.
     */
    public static void info() {
        CONSOLE_LOGGER.log(Level.INFO, "");
    }
}
