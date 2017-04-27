package org.lorislab.mechanic.targets;

import java.nio.file.Path;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
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
import org.lorislab.mechanic.service.ChangeLogHistory;
import org.lorislab.mechanic.service.Database;
import org.lorislab.mechanic.logger.Console;
import org.lorislab.mechanic.service.Server;
import org.lorislab.mechanic.util.VersionUtil;

/**
 *
 * @author Andrej Petras
 */
@Target(name = "update",
        requeredParameters = {
            "changeLogFile", "serverUrl", "dbUrl", "dbUser", "dbPassword", "dbLogTable", "template", "properties"
        }
)
@MetaInfServices(ExecutionTarget.class)
public class UpdateTarget extends AbstractUpdateTarget {

    @Override
    protected Set<String> processChanges(Parameters parameters, List<ChangeData> changes, Properties properties) {

        Set<String> propertyNames = new HashSet<>(properties.stringPropertyNames());
        Set<String> usedKeys = new HashSet<>(propertyNames);

        try (Database db = new Database(parameters);
                Server server = new Server(parameters)) {

            Map<String, String> checkSums = db.selectChangeLogHistoryCheckSum();
            boolean found = false;

            for (ChangeData change : changes) {

                LOGGER.log(Level.FINE, "[{0}] BEGIN author: {1} profile: {2}", new Object[]{change.getId(), change.getAuthor(), change.getProfile()});

                if (checkSums.containsKey(change.getId())) {
                    LOGGER.log(Level.FINE, "[{0}] The change already apply.", new Object[]{change.getId()});
                } else {
                    List<ChangeDataElement> elements = change.getElements();
                    // creata database history log
                    ChangeLogHistory chlh = create(change);
                    db.insertChangeLogHistory(chlh);

                    List<String> content = new LinkedList<>();
                    content.add("# Change: " + change.getId() + " profile: " + change.getProfile());

                    try {                        
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

                            // execute the CLI script
                            server.processCommands(content);
                        }

                        // commit history log in the database
                        db.commit();
                        Console.info("Execute change {0} success", change.getId());
                    } catch (Exception ex) {
                        db.rollback();

                        Console.info();
                        Console.info("- ERROR -------------------------------------------------------");
                        Console.info("Change:  {0}", change.getId());
                        Console.info("Error: {0}", ex.getMessage());
                        if (elements != null && !elements.isEmpty()) {
                            Console.info("Elements:");
                            elements.forEach((element) -> {
                                Console.info(" - {0}", element.getDebugLog());
                            });
                        } else {
                            Console.info("Files: is empty!");
                        }
                        if (!content.isEmpty()) {
                            Console.info("Content: ");
                            content.forEach((line) -> {
                                Console.info(line);
                            });
                        } else {
                            Console.info("Content: is empty!");
                        }

                        Console.info("- ERROR -------------------------------------------------------");

                        LOGGER.log(Level.SEVERE, "[{0}] error processing the CLI files", change.getId());

                        throw new RuntimeException("Error processing the CLI file change id: " + change.getId(), ex);
                    }
                }
                LOGGER.log(Level.FINE, "[{0}] END author: {1} profile: {2}", new Object[]{change.getId(), change.getAuthor(), change.getProfile()});
            }
            if (!found) {
                Console.info("No changes found");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not close the database!", ex);
        }
        return usedKeys;
    }

    private ChangeLogHistory create(ChangeData change) {
        ChangeLogHistory result = new ChangeLogHistory();
        result.setAuthor(change.getAuthor());
        result.setId(change.getId());
        result.setProfile(change.getProfile());
        result.setFileName(change.getParent().getPath().toAbsolutePath().toString());
        result.setExecuted(new Date());
        result.setVersion(VersionUtil.getVersion());
        return result;
    }
}
