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
package org.lorislab.mechanic;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * The main class.
 *
 * @author Andrej Petras
 */
public class Main {

    static {
        // wildfly embed-server configuration
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
    }

    /**
     * Parse the command lien arguments and start the application.
     *
     * @param args the list of command line arguments.
     */
    public static void main(String[] args) {

        // parse the command line arguments
        Map<String, String> parameters = new HashMap<>();
        if (args != null) {
            for (String arg : args) {
                String[] pp = arg.split("=");
                String value = null;
                if (pp.length == 2) {
                    value = pp[1];
                } else if (pp[0].startsWith("--")) {
                    value = Boolean.TRUE.toString();
                }
                parameters.put(pp[0], value);
            }
        }

        // set up the debug system property.
        System.setProperty("org.lorislab.application.debug", "" + parameters.containsKey("debug"));

        // start the application
        ServiceLoader<Application> loader = ServiceLoader.load(Application.class);
        loader.forEach((app) -> {
            app.init();
            app.run(parameters);
        });
    }
}
