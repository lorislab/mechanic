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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;

/**
 * The logger factory class.
 *
 * @author Andrej Petras
 */
public final class LoggerFactory {


    /* The simple date format for the logger. */
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss,SSS");
    /**
     * The log file name.
     */
    private static final String FILE_NAME = "mechanic-%g.log";

    /**
     * The handler instance.
     */
    private static Handler HANDLER;
    
    /**
     * The default constructor.
     */
    private LoggerFactory() {
        // empty cosntructor
    }

    /**
     * Gets the logger.
     *
     * @param clazz the log class.
     *
     * @return the logger for the class.
     */
    public static Logger getLogger(Class<?> clazz) {
        Logger result = Logger.getLogger(clazz.getName());
//        result.setUseParentHandlers(true);
        if (HANDLER == null) {
            HANDLER = createHandler();
        }
        result.addHandler(HANDLER);
        if (Boolean.getBoolean("org.lorislab.application.debug")) {
            result.addHandler(Console.getHandler());
        }
        result.setLevel(Level.ALL);
        return result;
    }

    /**
     * Creates the log handler.
     *
     * @return the log handler.
     */
    private static Handler createHandler() {
        Handler result = null;
        try {
            result = new FileHandler(FILE_NAME, 10485760, 5, true);
        } catch (Exception ex) {
            Logger.getLogger(LoggerFactory.class.getName()).log(Level.SEVERE, null, ex);
            result = new MemoryHandler();
        }
        result.setLevel(Level.ALL);
        result.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                String dateTime = FORMAT.format(new Date(record.getMillis()));
                StringBuilder sb = new StringBuilder();
                sb.append(dateTime);
                sb.append(' ');
                sb.append(record.getLevel());
                sb.append(" [");
                sb.append(record.getSourceClassName());
                sb.append('.');
                sb.append(record.getSourceMethodName());
                sb.append("] ");
                sb.append(formatMessage(record));
                sb.append("\n");
                return sb.toString();
            }
        });
        return result;
    }    
}
