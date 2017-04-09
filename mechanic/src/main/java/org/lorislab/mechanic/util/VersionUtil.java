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
import java.util.Properties;

/**
 * The version utility class.
 *
 * @author Andrej Petras
 */
public final class VersionUtil {

    /**
     * The resource version file.
     */
    private static final String RES = "/META-INF/version.properties";

    /**
     * The version of the application.
     */
    private static final String VERSION;

    /**
     * Loads the version from the class-path.
     */
    static {
        try (InputStream in = VersionUtil.class.getResourceAsStream(RES)) {
            Properties prop = new Properties();
            prop.load(in);
            VERSION = prop.getProperty("version");
        } catch (Exception e) {
            throw new RuntimeException("Error reading the version of the mechanic tool. Resource " + RES, e);
        }
    }

    /**
     * Default constructor.
     */
    private VersionUtil() {
        // do nothing
    }

    /**
     * Gets the version of the application.
     *
     * @return the version of the application.
     */
    public static String getVersion() {
        return VERSION;
    }
}
