package org.lorislab.mechanic.targets;

import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.kohsuke.MetaInfServices;
import org.lorislab.mechanic.annotation.Target;
import org.lorislab.mechanic.app.ExecutionTarget;
import org.lorislab.mechanic.app.MechanicApplication;
import org.lorislab.mechanic.app.Parameters;
import org.lorislab.mechanic.logger.Console;
import org.lorislab.mechanic.service.Server;

/**
 *
 * @author Andrej Petras
 */
@Target(name = "serverInfo",
        description = "Show the server version",
        requeredParameters = {
            "serverUrl"
        }
)
@MetaInfServices(ExecutionTarget.class)
public class ServerInfoTarget implements ExecutionTarget {

    @Override
    public void execute(MechanicApplication app, Parameters parameters) {
        try (Server server = new Server(parameters)) {
            final ModelNode op = Operations.createOperation("product-info",new ModelNode().setEmptyList());
            ModelNode result = server.processModelNode(op);
            Console.info(result.toJSONString(false));
        } catch (Exception ex) {
            throw new RuntimeException("Error reading the server version", ex);
        }
    }

}
