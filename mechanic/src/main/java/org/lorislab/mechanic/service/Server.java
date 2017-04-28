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
package org.lorislab.mechanic.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.as.cli.CliInitializationException;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandContextFactory;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.cli.Util;
import org.jboss.as.cli.impl.CommandContextConfiguration;
import org.jboss.as.cli.scriptsupport.CLI;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.lorislab.mechanic.app.Parameters;
import org.lorislab.mechanic.logger.LoggerFactory;

/**
 * The WILDFLY server client.
 *
 * @author Andrej Petras
 */
public class Server implements AutoCloseable {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    /**
     * The command context.
     */
    private CommandContext cmdCtx = null;

    /**
     * The embedded flag.
     */
    private final boolean embedded;

    /**
     * The default constructor.
     *
     * @param parameters the parameters.
     */
    public Server(Parameters parameters) {
        this(parameters.getServerUrl(),
                parameters.getServerUser(),
                parameters.getServerPassword(),
                parameters.getServerClientBind(),
                parameters.getServerClientTimeout());
    }

    public Server(String url, String username, String password, String bind, Integer timeout) {       
        if (url.startsWith("embed-server")) {
            connectEmbedded(url);
            embedded = true;
        } else {
            connect(url, username, password, bind, timeout);
            embedded = false;            
        }
    }

    public boolean isEmbedded() {
        return embedded;
    }

    private int connect(String url, String username, String password, String bind, Integer timeout) {
        LOGGER.log(Level.FINE, "Connect to remote server: {0}", url);
        int result = 0;

        final CommandContextConfiguration.Builder ctxBuilder = new CommandContextConfiguration.Builder();
        ctxBuilder.setErrorOnInteract(false);
//        ctxBuilder.setSilent(true);

        ctxBuilder.setConsoleOutput(new LoggerPrintStream());

        if (url != null && !url.isEmpty()) {
            ctxBuilder.setController(url);
        }
        if (username != null && !username.isEmpty()) {
            ctxBuilder.setUsername(username);
            ctxBuilder.setDisableLocalAuth(true);
        }
        if (password != null && !password.isEmpty()) {
            ctxBuilder.setPassword(password.toCharArray());
        }

//        String nolocalauth = info.getProperty(Config.CONFIG_BASE + ".no-local-auth");
//        if (nolocalauth != null && nolocalauth.equals(Boolean.TRUE.toString())) {
//            ctxBuilder.setDisableLocalAuth(true);
//        }
//        String erroroninteract = info.getProperty(Config.CONFIG_BASE + ".error-on-interact");
//        if (erroroninteract != null && erroroninteract.equals(Boolean.TRUE.toString())) {
//            ctxBuilder.setErrorOnInteract(true);
//        }
        if (bind != null && !bind.isEmpty()) {
            ctxBuilder.setClientBindAddress(bind);
        }

        int connectionTimeout = -1;
        if (timeout != null) {
            connectionTimeout = timeout;
        }
        ctxBuilder.setConnectionTimeout(connectionTimeout);
        ctxBuilder.setSilent(true);

        try {
            CommandContextConfiguration ctx = ctxBuilder.build();
            cmdCtx = CommandContextFactory.getInstance().newCommandContext(ctx);
            try {
                cmdCtx.connectController();
            } catch (CommandLineException e) {
                throw new CliInitializationException("Failed to connect to the controller", e);
            }
        } catch (Throwable t) {
            String msg = Util.getMessagesFromThrowable(t);
            LOGGER.log(Level.SEVERE, msg);
            result = 1;
            throw new RuntimeException("Error connect to the server: " + msg);
        }
        return result;
    }

    public void processCommands(List<String> commands) throws Exception {
        int i = 0;
        try (PrintStream ps = new PrintStream(new ByteArrayOutputStream())) {
            cmdCtx.captureOutput(ps);

            while (checkStatus() && i < commands.size()) {
                String command = commands.get(i);
                LOGGER.fine(command);
                cmdCtx.handle(command);
                ++i;
            }
        } catch (CommandLineException ex) {
            StringBuilder sb = new StringBuilder();
            CommandLineException e = ex;
            while (e != null) {
                sb.append(e.getMessage());
                Throwable t = e.getCause();
                if (t instanceof CommandLineException) {
                    e = (CommandLineException) t;
                    sb.append('\n');
                } else {
                    e = null;
                }
            }
            String t = sb.toString();
            LOGGER.fine(t);
            throw new Exception(t);
        } finally {
            cmdCtx.releaseOutput();
        }
    }

    public ModelNode processModelNode(ModelNode op) {
        LOGGER.fine(op.toJSONString(false));
        ModelControllerClient client = cmdCtx.getModelControllerClient();
        ModelNode result = null;
        try {
            result = client.execute(op);
        } catch (Exception e) {
            throw new RuntimeException("Could not execute operation " + op.toJSONString(false), e);
        }
        if (result != null) {
            LOGGER.fine(result.toJSONString(false));
            if (!Operations.isSuccessfulOutcome(result)) {
                String value = Operations.getFailureDescription(result).asString();
                throw new RuntimeException("Error execute the command! Error: " + value);
            }
        }
        return result;
    }

    private boolean checkStatus() {
        return cmdCtx.getExitCode() == 0 && !cmdCtx.isTerminated();
    }

    private int connectEmbedded(String command) {
        LOGGER.log(Level.FINE, "Start wildfly server in the embedded mode: {0}", command);
        int result = 0;
        try {
            CLI c = CLI.newInstance();
            cmdCtx = c.getCommandContext();
            c.cmd(command);
        } catch (Throwable t) {
            String msg = Util.getMessagesFromThrowable(t);
            LOGGER.log(Level.SEVERE, msg);
            result = 1;
            throw new RuntimeException("Error connect to the server: " + msg);
        }
        return result;
    }

    public boolean isClosed() {
        return cmdCtx.isTerminated();
    }

    @Override
    public void close() throws IOException {
        if ((cmdCtx != null)) {
            cmdCtx.terminateSession();
        }
    }

    public class LoggerPrintStream extends PrintStream {

        public LoggerPrintStream() {
            super(new ByteArrayOutputStream(), true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void print(String s) {
            LOGGER.info(s);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void println(String s) {
            LOGGER.info(s);
        }

    }

}
