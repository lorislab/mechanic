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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.lorislab.mechanic.service.Database;
import org.lorislab.mechanic.logger.Console;

/**
 *
 * @author Andrej Petras
 */
@Target(name = "updateCli",
        requeredParameters = {
            "changeLogFile", "template", "dbUrl", "dbUser", "dbPassword", "dbLogTable", "properties"
        }
)
@MetaInfServices(ExecutionTarget.class)
public class UpdateCliTarget extends AbstractUpdateTarget {

    @Override
    protected Set<String> processChanges(Parameters parameters, List<ChangeData> changes, Properties properties) {

        Set<String> propertyNames = new HashSet<>(properties.stringPropertyNames());
        Set<String> usedKeys = new HashSet<>(propertyNames);

        try (Database db = new Database(parameters)) {

            Map<String, String> checkSums = db.selectChangeLogHistoryCheckSum();
            boolean found = false;
            
            for (ChangeData change : changes) {
                LOGGER.log(Level.FINE, "[{0}] BEGIN author: {1} profile: {2}", new Object[]{change.getId(), change.getAuthor(), change.getProfile()});

                if (checkSums.containsKey(change.getId())) {
                    LOGGER.log(Level.FINE, "[{0}] The change {0} already apply.", new Object[]{change.getId()});
                } else {
                    List<String> content = new ArrayList<>();
                    content.add("# Change: " + change.getId() + " profile: " + change.getProfile());
                        
                    
                    List<ChangeDataElement> elements = change.getElements();
                    if (elements == null || elements.isEmpty()) {
                        LOGGER.log(Level.FINE, "[{0}] no CLI elements define in the change", change.getId());
                        content.add("# No cli elements ");
                    } else {
                            found = true;

                            if (change.isBatch()) {
                                content.add("batch");
                            }
                            
                            for (ChangeDataElement element : elements) {
                                List<String> lines = element.createCli();
                                for (String line : lines) {
                                    String tmp = ExpressionDataService.processExpressions(line, properties, new HashSet<>(propertyNames), usedKeys);
                                    content.add(tmp);
                                }
                            }

                            if (change.isBatch()) {
                                content.add("run-batch");
                            }    
                    }                    
                    
                    content.forEach((item) -> {
                        Console.info(item);
                    });                    
                }
                LOGGER.log(Level.FINE, "[{0}] END  author: {1} profile: {2}", new Object[]{change.getId(), change.getAuthor(), change.getProfile()});
            }
            if (!found) {
                Console.info("No changes found");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not close the database!", ex);
        }
        return usedKeys;
    }

}
