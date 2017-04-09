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

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.MetaInfServices;
import org.lorislab.mechanic.annotation.Target;
import org.lorislab.mechanic.logger.Console;
import org.lorislab.mechanic.app.ExecutionTarget;
import org.lorislab.mechanic.app.ExecutionTargetMetaData;
import org.lorislab.mechanic.app.MechanicApplication;
import org.lorislab.mechanic.app.ParameterMetaData;
import org.lorislab.mechanic.app.Parameters;
import org.lorislab.mechanic.util.VersionUtil;

/**
 *
 * @author Andrej Petras
 */
@Target(name = "")
@MetaInfServices(ExecutionTarget.class)
public class DefaultTarget implements ExecutionTarget {

    @Override
    public void execute(MechanicApplication app, Parameters parameters) {
        if (parameters.isVersion()) {
            Console.info("Mechanic version: {0}", VersionUtil.getVersion());
        } else if (parameters.isHelp()) {
            showHelp(app);
        } else {
            throw new RuntimeException("Missing target or parameter to execute");
        }
    }

    private void showHelp(MechanicApplication app) {
        Console.info("Execution command:");
        Console.info("mechanic [ PARAMETERS ] [ COMMANDS ]");
        Console.info();
        Console.info("Standard Commands:");
        List<ExecutionTargetMetaData> targets = app.getTargets();
        if (targets != null) {
            for (int i = 0; i < targets.size(); i++) {
                ExecutionTargetMetaData target = targets.get(i);
                if (target.getDescription() != null && !target.getDescription().isEmpty()) {
                    Console.info(String.format("%4s %-20s %-15s", "", target.getName(), target.getDescription()));
                }
            }
        }
        Console.info();
        Console.info("Parameters:");

        Parameters pa = new Parameters();
        List<ParameterMetaData> params = app.getParameters();

        for (int i = 0; i < params.size(); i++) {
            ParameterMetaData p = params.get(i);
            Console.info(String.format("%4s %-20s %-15s", "", p.getParameter(), p.getDescription()));
            Field f = p.getField();
            f.setAccessible(true);
            try {
                Object value = f.get(pa);
                if (value != null) {
                    Console.info(String.format("%25s %-8s %s", "", "default:", value));
                }
            } catch (Exception ex) {
                Logger.getLogger(DefaultTarget.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
