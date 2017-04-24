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
package org.lorislab.mechanic.targets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.lorislab.mechanic.app.ExecutionTarget;
import org.lorislab.mechanic.app.MechanicApplication;
import org.lorislab.mechanic.app.Parameters;
import org.lorislab.mechanic.data.ChangeData;
import org.lorislab.mechanic.data.ChangeLogData;
import org.lorislab.mechanic.data.ChangeLogDataService;
import org.lorislab.mechanic.data.ExpressionDataService;
import org.lorislab.mechanic.logger.Console;
import org.lorislab.mechanic.logger.LoggerFactory;
import org.lorislab.mechanic.util.FileUtil;

/**
 *
 * @author Andrej Petras
 */
public abstract class AbstractUpdateTarget implements ExecutionTarget {

    private static final Charset CHAR_SET = Charset.forName("UTF-8");

    private static final Pattern LTRIM = Pattern.compile("\\s+$");

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    protected abstract Set<String> processChanges(Parameters parameters, List<ChangeData> changes, Properties properties);

    protected void validateParameters(Parameters parameters) {

    }

    protected void validateUsedKeys(Set<String> usedKeys, Properties properties) {

    }

    @Override
    public void execute(MechanicApplication app, Parameters parameters) {

        // validate special parameters
        validateParameters(parameters);

        // load the change log data
        ChangeLogData data = ChangeLogDataService.loadChangeLogData(parameters.getChangeLogFile(), parameters.getProfiles());

        // check the properties
        Properties properties = checkProperties(parameters);

        // process changes
        LOGGER.log(Level.FINE, "Load the cli files and apply the properties");
        List<ChangeData> changes = data.getChanges();
        if (changes == null || changes.isEmpty()) {
            LOGGER.log(Level.FINE, "No changes found for the update");
        } else {

            // process changes
            Set<String> usedKeys = processChanges(parameters, changes, properties);

            // check not used keys in the CLI scripts
            validateUsedKeys(usedKeys, properties);
        }
    }

    protected Properties checkProperties(Parameters parameters) {

        // load cli template properties
        LOGGER.log(Level.FINE, "Load cli template properties: {0}", parameters.getTemplate());
        Properties templateProperites = FileUtil.loadProperties(parameters.getTemplate());

        // check the cli properties
        Properties properties = templateProperites;
        if (parameters.getProperties() == null) {
            LOGGER.fine("The cli  properties are null switch to the template properties for the cli scripts.");
        } else {
            LOGGER.log(Level.FINE, "Load cli properties: {0}", parameters.getProperties());

            if (!parameters.isSkipTemplatePropertiesCheck() && parameters.getProperties().equals(parameters.getTemplate())) {
                throw new RuntimeException("The template and properties files are the same!");
            }
            properties = FileUtil.loadProperties(parameters.getProperties());
        }

        // diff the properties
        Set<String> temp = new HashSet<>(templateProperites.stringPropertyNames());
        temp.removeAll(properties.stringPropertyNames());
        if (!temp.isEmpty()) {
            Console.error("Missing properties:");
            temp.forEach((item) -> {
                Console.error("[ + ] {0}={1}", item, templateProperites.getProperty(item));
            });
            throw new RuntimeException("Missing properties!!");
        }

        Set<String> prop = new HashSet<>(properties.stringPropertyNames());
        prop.removeAll(templateProperites.stringPropertyNames());
        if (!prop.isEmpty()) {
            if (parameters.isSkipPropWarn()) {
                Console.warn("Deprecated properties:");
                for (String item : prop) {
                    Console.warn("[ - ] {0}={1}", item, properties.getProperty(item));
                }
            } else {
                Console.error("Deprecated properties:");
                for (String item : prop) {
                    Console.error("[ - ] {0}={1}", item, properties.getProperty(item));
                }
                throw new RuntimeException("Deprecated properties found! To skip this validation use the --skipPropWarn parameter");                
            }
        }

        return properties;
    }

    protected List<String> loadCli(final Path cli, final Properties properties, final Set<String> propertiesKeys, final Set<String> usedKeys) throws Exception {
        List<String> result = new LinkedList<>();
        try (InputStream sqlStream = Files.newInputStream(cli)) {
            if (sqlStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(sqlStream, CHAR_SET))) {
                    String line = reader.readLine();
                    while (line != null) {
                        line = LTRIM.matcher(line).replaceAll("");
                        line = ExpressionDataService.processExpressions(line, properties, new HashSet<>(propertiesKeys), usedKeys);
                        result.add(line);
                        line = reader.readLine();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading the cli file: " + cli, e);
        }

        return result;
    }

}
