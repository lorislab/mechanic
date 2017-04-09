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

import java.util.Map;

/**
 * The application interface.
 *
 * @author Andrej Petras
 */
public interface Application {

    /**
     * The initialize method of the application.
     */
    void init();

    /**
     * The main execution method.
     *
     * @param args the map of command line arguments.
     */
    void run(Map<String, String> args);
}
