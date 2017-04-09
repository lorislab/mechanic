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
package org.lorislab.mechanic.app;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;
import org.kohsuke.MetaInfServices;
import org.lorislab.mechanic.Application;
import org.lorislab.mechanic.annotation.Parameter;
import org.lorislab.mechanic.annotation.PropertyFile;
import org.lorislab.mechanic.annotation.Target;
import org.lorislab.mechanic.app.ParameterMetaData.Type;
import org.lorislab.mechanic.logger.Console;
import org.lorislab.mechanic.util.FileUtil;

/**
 * The main application.
 *
 * @author Andrej Petras
 */
@MetaInfServices
public class MechanicApplication implements Application {

    /**
     * The map of the execution targets meta data.
     */
    private final Map<String, ExecutionTargetMetaData> targets = new HashMap<>();

    /**
     * The map of the parameters meta data.
     */
    private final Map<String, ParameterMetaData> parameters = new HashMap<>();

    /**
     * The property file.
     */
    private PropertyFile propertyFile;

    /**
     * Gets the list of all parameters meta data.
     *
     * @return the list of all parameters meta data.
     */
    public List<ParameterMetaData> getParameters() {
        return Collections.unmodifiableList(new ArrayList<>(parameters.values()));
    }

    /**
     * Gets the list of all targets meta data.
     *
     * @return the list of all targets meta data.
     */
    public List<ExecutionTargetMetaData> getTargets() {
        return Collections.unmodifiableList(new ArrayList<>(targets.values()));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void init() {

        // load targets
        ServiceLoader<ExecutionTarget> loader = ServiceLoader.load(ExecutionTarget.class);
        loader.forEach((service) -> {

            Class<? extends ExecutionTarget> clazz = service.getClass();
            Target target = clazz.getAnnotation(Target.class);
            if (target != null) {
                ExecutionTargetMetaData ed = new ExecutionTargetMetaData();
                ed.setName(target.name());
                ed.setDescription(target.description());
                ed.setTarget(service);
                String[] rp = target.requeredParameters();
                if (rp != null && rp.length > 0) {
                    ed.setRequiredParameters(new ArrayList<>(Arrays.asList(rp)));
                }
                targets.put(target.name(), ed);
            }
        });

        // load parameters
        propertyFile = Parameters.class.getAnnotation(PropertyFile.class);

        Type[] types = Type.values();
        Field[] fields = Parameters.class.getDeclaredFields();
        if (fields != null) {
            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    Parameter ano = field.getAnnotation(Parameter.class);
                    if (ano != null) {
                        ParameterMetaData pd = new ParameterMetaData();
                        pd.setName(ano.name());
                        pd.setParameter("--" + ano.name());
                        pd.setDescription(ano.description());
                        pd.setProperty(ano.property());
                        pd.setField(field);

                        Type type = null;
                        Class clazz = field.getType();
                        for (int i = 0; i < types.length && type == null; i++) {
                            if (types[i].isClazz(clazz)) {
                                type = types[i];
                            }
                        }
                        if (type == null) {
                            throw new RuntimeException("Not supported type of the parameter: " + clazz.getName());
                        }
                        pd.setType(type);

                        field.setAccessible(true);
                        parameters.put(pd.getName(), pd);
                    }
                }
            }
        }
    }

    /**
     * Creates the application parameters corresponding to the input parameters.
     *
     * @param args the input parameters.
     * @return the application parameters.
     */
    private Parameters createParameters(final Map<String, String> args) {

        Parameters result = new Parameters();

        String pfn = args.get(propertyFile.parameter());
        if (pfn == null || pfn.isEmpty()) {
            pfn = propertyFile.file();
        }
        result.setConfig(Paths.get(pfn));

        final Properties prop = new Properties();
        if (Files.exists(result.getConfig())) {
            FileUtil.loadProperties(result.getConfig(), prop);
        }

        parameters.forEach((key, metadata) -> {

            // load parameter from command line
            String value = args.remove(metadata.getParameter());
            if (value == null && metadata.isProperty()) {
                // load parameter from property
                value = prop.getProperty(metadata.getName());
            }
            prop.remove(metadata.getName());

            if (value != null && !value.isEmpty()) {
                Object tmp = null;
                Class clazz = metadata.getField().getType();
                switch (metadata.getType()) {
                    case STRING:
                        tmp = value;
                        break;
                    case LIST:
                        String[] list = value.split(",");
                        tmp = new ArrayList<>(Arrays.asList(list));
                        break;
                    case INTEGER:
                        tmp = Integer.valueOf(value);
                        break;
                    case BOOLEAN:
                        tmp = Boolean.parseBoolean(value);
                        break;
                    case PATH:
                        tmp = Paths.get(value);
                        break;
                }

                try {
                    metadata.getField().set(result, tmp);
                } catch (Exception ex) {
                    throw new RuntimeException("Internal error. Could not set the value to the parameters", ex);
                }
            }
        });

        // check not supported parameters
        if (!prop.isEmpty()) {
            prop.forEach((t, u) -> {
                Console.info("Ignore property: {0}={1}", t, u);
            });
        }
        return result;
    }

    /**
     * Creates the execution target corresponding to the input parameters.
     *
     * @param args the input parameters.
     * @return the corresponding execution target.
     */
    private ExecutionTargetMetaData createExecutionTarget(Map<String, String> args) {
        ExecutionTargetMetaData result = null;
        for (Entry<String, ExecutionTargetMetaData> e : targets.entrySet()) {
            if (args.remove(e.getKey(), null)) {
                if (result == null) {
                    result = e.getValue();
                } else {
                    throw new RuntimeException("Second execution target found. Execution target: " + result.getName() + " second target: " + e.getKey());
                }
            }
        }
        if (result == null) {
            result = targets.get("");
        }
        return result;
    }

    /**
     * Checks required parameters.
     *
     * @param exec the execution target.
     * @param args the input parameters.
     */
    private void checkRequiredParameters(ExecutionTargetMetaData exec, Parameters args) {
        List<String> rps = exec.getRequiredParameters();
        if (rps != null && !rps.isEmpty()) {
            rps.forEach((rp) -> {
                ParameterMetaData param = parameters.get(rp);
                if (param == null) {
                    throw new RuntimeException("Wrong required parameter name: '" + rp + "' for the target '" + exec.getName());
                }
                Field f = param.getField();
                Object value = null;
                try {
                    value = f.get(args);
                } catch (IllegalAccessException | IllegalArgumentException ex) {
                    throw new RuntimeException("Error reading the required parameter '" + rp + "' value!", ex);
                }
                if (value == null) {
                    throw new RuntimeException("The parameter '" + rp + "' for the target '" + exec.getName() + "' is required!");
                } else {
                    if (param.getType() == Type.PATH) {
                        Path tmp = (Path) value;
                        if (!Files.exists(tmp)) {
                            throw new RuntimeException("The file of the parameter '" + rp + "' for the target '" + exec.getName() + "' does not exists! File: " + tmp.toAbsolutePath());
                        }
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void run(Map<String, String> args) {
        try {
            // load command line parameters
            Parameters tmp = createParameters(args);

            // load the target
            ExecutionTargetMetaData execMetaData = createExecutionTarget(args);

            // check not supported parameters
            if (!args.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                args.forEach((t, u) -> {
                    sb.append(' ').append(t);
                    if (u != null) {
                        sb.append('=').append(u);
                    }
                });
                throw new RuntimeException("Not supported parameters/target found: " + sb.toString());
            }

            // check required parameters
            checkRequiredParameters(execMetaData, tmp);

            // replace properties by command line parameters
            execMetaData.getTarget().execute(this, tmp);

        } catch (Exception e) {
            Console.info();
            Console.error("Error executing the mechanic tool.", e);
        }
    }

}
