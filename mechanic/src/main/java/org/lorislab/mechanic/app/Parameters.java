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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.lorislab.mechanic.annotation.Parameter;
import org.lorislab.mechanic.annotation.PropertyFile;

/**
 * The application parameter.
 * 
 * @author Andrej Petras
 */
@Getter
@Setter
@ToString
@PropertyFile(parameter="config", file = "mechanic.properties")
public class Parameters {

    /**
     * The change log file.
     */
    @Parameter(
            name = "changeLogFile",
            description = "The path to the change log file"
    )
    private Path changeLogFile;

    /**
     * The configuration file.
     */
    @Parameter(
            name = "config",
            description = "The mechanic configuration file"
    )
    private Path config;

    /**
     * The properties file.
     */
    @Parameter(
            name = "properties",
            property = true,
            description = "The cli script properties"
    )
    private Path properties;

    /**
     * The template file.
     */
    @Parameter(
            name = "template",
            property = true,
            description = "The cli script template properties"
    )
    private Path template;

    /**
     * Skip the template and properties check.
     */
    @Parameter(
            name = "skipTemplatePropertiesCheck",
            property = true,
            description = "Skip the validation for same properties and template file"
    )    
    private boolean skipTemplatePropertiesCheck;
    
    /**
     * The server URL.
     */
    @Parameter(
            name = "serverUrl",
            property = true,
            description = "The server URL"
    )
    private String serverUrl;

    /**
     * The server user.
     */
    @Parameter(
            name = "serverUser",
            property = true,
            description = "The server connection user name"
    )
    private String serverUser;

    /**
     * The server password.
     */
    @Parameter(
            name = "serverPassword",
            property = true,
            description = "The server connection password"
    )
    private String serverPassword;

    /**
     * The server client binding.
     */
    @Parameter(
            name = "serverClientBind",
            property = true,
            description = "The server client binding"
    )
    private String serverClientBind;

    /**
     * The server client timeout.
     */
    @Parameter(
            name = "serverClientTimeout",
            property = true,
            description = "The server client timeout"
    )
    private Integer serverClientTimeout;

    /**
     * The list of profiles.
     */
    @Parameter(
            name = "profiles",
            property = true,
            description = "The list of change set profiles"
    )
    private List<String> profiles = new ArrayList<>();

    /**
     * The database URL.
     */
    @Parameter(
            name = "dbUrl",
            property = true,
            description = "The database for changes"
    )
    private String dbUrl = "jdbc:h2:~/mechanic";

    /**
     * The database user.
     */
    @Parameter(
            name = "dbUser",
            property = true,
            description = "The database user"
    )
    private String dbUser = "sa";

    /**
     * The database password.
     */
    @Parameter(
            name = "dbPassword",
            property = true,
            description = "The database password"
    )
    private String dbPassword = "sa";

    /**
     * The database log table name.
     */
    @Parameter(
            name = "dbLogTable",
            property = true,
            description = "The database log table"
    )
    private String dbLogTable = "MECHANIC_LOG";

    /**
     * The database SQL where parameter.
     */
    @Parameter(
            name = "dbSqlWhere",
            property = false,
            description = "Show the history table by the 'WHERE' statment"
    )
    private String dbSqlWhere;

    /**
     * The database clean table name.
     */
    @Parameter(
            name = "dbCleanTable",
            property = false,
            description = "The history table to be clean up"
    )
    private String dbCleanTable;

    /**
     * Show version flag.
     */
    @Parameter(
            name = "version",
            property = false,
            description = "Show the version of the tool"
    )
    private boolean version;

    /**
     * Show help flag.
     */
    @Parameter(
            name = "help", 
            property = false,
            description = "Show this help"
    )
    private boolean help;

    /**
     * Skip properties warning.
     */
    @Parameter(
            name = "skipPropWarn", 
            property = false,
            description = "Skip properties diff warnings"
    )
    private boolean skipPropWarn;

    /**
     * The debug flag.
     */
    @Parameter(
            name = "debug", 
            property = false,
            description = "Show the debug logs"
    )
    private boolean debug;
}
