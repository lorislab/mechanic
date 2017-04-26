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

import java.text.SimpleDateFormat;
import java.util.List;
import org.kohsuke.MetaInfServices;
import org.lorislab.mechanic.annotation.Target;
import org.lorislab.mechanic.app.ExecutionTarget;
import org.lorislab.mechanic.app.MechanicApplication;
import org.lorislab.mechanic.app.Parameters;
import org.lorislab.mechanic.service.ChangeLogHistory;
import org.lorislab.mechanic.service.Database;
import org.lorislab.mechanic.logger.Console;

/**
 *
 * @author Andrej Petras
 */
@Target(name = "dbShowLog",
        requeredParameters = {
            "dbUrl", "dbUser", "dbPassword", "dbLogTable"
        }
)
@MetaInfServices(ExecutionTarget.class)
public class DbShowLogTarget implements ExecutionTarget {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    @Override
    public void execute(MechanicApplication app, Parameters parameters) {
        try (Database db = new Database(parameters)) {

            String sql = parameters.getDbSqlWhere();

            List<ChangeLogHistory> result;
            if (sql != null && !sql.isEmpty()) {
                result = db.selectChangeLogHistory(sql);
            } else {
                result = db.selectChangeLogHistory();
            }

            if (result == null || result.isEmpty()) {
                Console.info("No result found!");
            } else {

                Console.info("ID;AUTHOR;EXECUTED;ORDER;TYPE;SUM;DESC;COMMENTS;PROFILE;VERSION;FILENAME");

                result.stream().map((log) -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(log.getId()).append(';');
                    sb.append(log.getAuthor()).append(';');
                    sb.append(SDF.format(log.getExecuted())).append(';');
                    sb.append(log.getOrder()).append(';');
                    sb.append(log.getType()).append(';');
                    sb.append(log.getSum()).append(';');
                    sb.append(log.getDesc()).append(';');
                    sb.append(log.getComments()).append(';');
                    sb.append(log.getProfile()).append(';');
                    sb.append(log.getVersion()).append(';');
                    sb.append(log.getFileName());
                    return sb;
                }).forEachOrdered((sb) -> {
                    Console.info(sb.toString());
                });
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not close the database!", ex);
        }
    }

}
