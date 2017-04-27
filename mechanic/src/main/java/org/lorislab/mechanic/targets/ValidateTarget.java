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

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import org.kohsuke.MetaInfServices;
import org.lorislab.mechanic.annotation.Target;
import org.lorislab.mechanic.app.ExecutionTarget;
import org.lorislab.mechanic.app.Parameters;
import org.lorislab.mechanic.data.ChangeData;
import org.lorislab.mechanic.data.ExpressionDataService;
import org.lorislab.mechanic.data.elements.ChangeDataElement;
import org.lorislab.mechanic.logger.Console;

/**
 *
 * @author Andrej Petras
 */
@Target(name = "validate",
        requeredParameters = {
            "changeLogFile", "template", "properties"
        }
)
@MetaInfServices(ExecutionTarget.class)
public class ValidateTarget extends AbstractUpdateTarget {

    @Override
    protected void validateUsedKeys(Set<String> usedKeys, Properties properties) {
        if (!usedKeys.isEmpty()) {
            Console.warn("Deprecated template properties:");
            usedKeys.forEach((item) -> {
                Console.warn("[ - ] {0}={1}", item, properties.getProperty(item));
            });
        }
    }

    @Override
    protected Set<String> processChanges(Parameters parameters, List<ChangeData> changes, Properties properties) {

        Set<String> propertyNames = new HashSet<>(properties.stringPropertyNames());
        Set<String> usedKeys = new HashSet<>(propertyNames);

        for (ChangeData change : changes) {
            LOGGER.log(Level.FINE, "[{0}] BEGIN author: {1} profile: {2}", new Object[]{change.getId(), change.getAuthor(), change.getProfile()});

            List<ChangeDataElement> elements = change.getElements();
            if (elements == null || elements.isEmpty()) {
                LOGGER.log(Level.FINE, "[{0}] no CLI elements define in the change", change.getId());
            } else {
                for (ChangeDataElement element : elements) {
                    try {
                        List<String> lines = element.createCli();
                        for (String line : lines) {
                            ExpressionDataService.processExpressions(line, properties, new HashSet<>(propertyNames), usedKeys);
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "[{0}] error reading the CLI element {1}", new Object[]{change.getId(), element});
                        throw new RuntimeException("Error reading the CLI element " + element, ex);
                    }
                }

            }

            LOGGER.log(Level.FINE, "[{0}] END author: {1} profile: {2}", new Object[]{change.getId(), change.getAuthor(), change.getProfile()});
        }
        return usedKeys;
    }

}
