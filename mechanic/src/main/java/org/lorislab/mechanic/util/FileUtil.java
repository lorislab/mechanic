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
package org.lorislab.mechanic.util;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * The file utility class
 *
 * @author Andrej Petras
 */
public final class FileUtil {

    /**
     * The default constructor.
     */
    private FileUtil() {
        // empty constructor
    }

    /**
     * Loads the properties file.
     *
     * @param file the property file.
     * @return the properties.
     */
    public static Properties loadProperties(String file) {
        return loadProperties(Paths.get(file));
    }

    /**
     * Loads the properties file.
     *
     * @param path the path to the property file.
     * @return the properties.
     */
    public static Properties loadProperties(Path path) {
        return loadProperties(path, new Properties());
    }

    /**
     * Loads the properties file.
     *
     * @param path the path to the property file.
     * @param result the result properties.
     * @return the properties.
     */
    public static Properties loadProperties(Path path, Properties result) {
        if (!Files.exists(path)) {
            throw new RuntimeException("The property file does not exist! Path: " + path);
        }
        try (InputStream in = Files.newInputStream(path)) {
            result.load(in);
        } catch (Exception ex) {
            throw new RuntimeException("Error loading the properties from file: " + path, ex);
        }
        return result;
    }
}
