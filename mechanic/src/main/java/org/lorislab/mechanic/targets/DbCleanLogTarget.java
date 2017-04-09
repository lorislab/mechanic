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

import org.kohsuke.MetaInfServices;
import org.lorislab.mechanic.annotation.Target;
import org.lorislab.mechanic.app.ExecutionTarget;
import org.lorislab.mechanic.app.MechanicApplication;
import org.lorislab.mechanic.app.Parameters;
import org.lorislab.mechanic.service.Database;

/**
 *
 * @author Andrej Petras
 */
@Target(name = "dbCleanLog",
        description = "Show the change log history information",
        requeredParameters = {
            "dbUrl", "dbUser", "dbPassword","dbLogTable","dbCleanTable"
        }
)
@MetaInfServices(ExecutionTarget.class)
public class DbCleanLogTarget implements ExecutionTarget {

    @Override
    public void execute(MechanicApplication app, Parameters parameters) {
        
        String table = parameters.getDbLogTable();
        String tmp = parameters.getDbCleanTable();
        if (tmp == null || !tmp.equals(table)) {
            throw new RuntimeException("To clean up the history log table the parameter 'dbCleanTable' needs to equals the 'dbLogTable=" + parameters.getDbLogTable() + "' table.");
        }
        
        try (Database db = new Database(parameters)) {
            db.cleanTable();
            db.commit();
        } catch (Exception ex) {
            throw new RuntimeException("Could not close the database!", ex);
        }
    }

}
