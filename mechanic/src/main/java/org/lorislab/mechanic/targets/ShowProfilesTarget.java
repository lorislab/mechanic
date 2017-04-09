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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.MetaInfServices;
import org.lorislab.mechanic.annotation.Target;
import org.lorislab.mechanic.app.ExecutionTarget;
import org.lorislab.mechanic.app.MechanicApplication;
import org.lorislab.mechanic.app.Parameters;
import org.lorislab.mechanic.data.ChangeData;
import org.lorislab.mechanic.data.ChangeLogData;
import org.lorislab.mechanic.data.ChangeLogDataService;
import static org.lorislab.mechanic.data.ChangeLogDataService.ALL_PROFILES;
import org.lorislab.mechanic.logger.Console;
import org.lorislab.mechanic.logger.LoggerFactory;

/**
 *
 * @author Andrej Petras
 */
@Target(name = "showProfiles", description = "Show the list of used profiles",
        requeredParameters = {
            "changeLogFile"
        }
)
@MetaInfServices(ExecutionTarget.class)
public class ShowProfilesTarget implements ExecutionTarget {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ShowProfilesTarget.class);

    @Override
    public void execute(MechanicApplication app, Parameters parameters) {

        // load the change log data
        List<String> tmp = Arrays.asList(ALL_PROFILES);
        ChangeLogData data = ChangeLogDataService.loadChangeLogData(parameters.getChangeLogFile(), tmp);

        // process changes
        LOGGER.log(Level.FINE, "Load the cli files and apply the properties");
        List<ChangeData> changes = data.getChanges();
        if (changes == null || changes.isEmpty()) {
            LOGGER.log(Level.FINE, "No changes found for the update");
        } else {
            List<String> profiles = new LinkedList<>();
            for (ChangeData change : changes) {
                String profile = change.getProfile();
                if (profile != null && !profile.isEmpty()) {
                    profiles.add(profile);
                }
            }
            
            if (profiles.isEmpty()) {
                Console.info("No profiles found!");
            } else {
                Console.info("Profiles:");
                for (String profile : profiles) {
                    Console.info(profile);
                }
            }
        }
    }

}
