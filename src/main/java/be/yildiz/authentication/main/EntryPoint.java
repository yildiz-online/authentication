/*
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 * Copyright (c) 2017 Grégory Van den Borre
 *
 * More infos available: https://www.yildiz-games.be
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT  HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE  SOFTWARE.
 */

package be.yildiz.authentication.main;

import be.yildiz.authentication.AccountCreationManager;
import be.yildiz.authentication.AuthenticationManager;
import be.yildiz.authentication.DataBaseAuthenticator;
import be.yildiz.authentication.DatabaseAccountCreator;
import be.yildiz.authentication.configuration.Configuration;
import be.yildiz.authentication.network.AsynchronousAuthenticationServer;
import be.yildiz.authentication.network.AuthenticationServer;
import be.yildiz.authentication.network.JavaMailEmailService;
import be.yildiz.common.Terminal;
import be.yildiz.common.authentication.AuthenticationRules;
import be.yildiz.module.database.DataBaseConnectionProvider;
import be.yildiz.module.database.DatabaseConnectionProviderFactory;
import be.yildiz.module.database.DatabaseUpdater;
import be.yildiz.module.database.LiquibaseDatabaseUpdater;
import be.yildiz.module.messaging.Broker;
import be.yildiz.module.messaging.BrokerMessageDestination;
import be.yildiz.module.messaging.MessageProducer;
import be.yildiz.module.network.server.SanityServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Application entry point, contains the main method.
 *
 * @author Grégory Van den Borre
 */
public final class EntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryPoint.class);


    private EntryPoint() {
        super();
    }

    /**
     * Register a new log file, the connection properties to the database from a property file, launch the 2 network servers, authentication server, and token validation server, and start listening
     * for client requests.
     *
     * @param args Unused.
     */
    public static void main(String[] args) {
        try {
            LOGGER.debug("Debug logger level enabled.");
            Configuration config = Configuration.fromAppArgs(args);

            LOGGER.info("Preparing the database connection...");
            try(DataBaseConnectionProvider provider = DatabaseConnectionProviderFactory.getInstance().createWithHighPrivilege(config)) {
                provider.sanity();
                DatabaseUpdater databaseUpdater = LiquibaseDatabaseUpdater.fromConfigurationPath("authentication-database-update.xml");
                databaseUpdater.update(provider);
                Broker broker = Broker.initialize(config);
                BrokerMessageDestination accountCreatedQueue = broker.registerQueue("authentication-creation");
                MessageProducer producer = accountCreatedQueue.createProducer();
                AuthenticationManager manager = new AuthenticationManager(new DataBaseAuthenticator(provider));
                AccountCreationManager accountCreationManager =
                        new AccountCreationManager(new DatabaseAccountCreator(provider, producer), AuthenticationRules.DEFAULT, new JavaMailEmailService(config));
                LOGGER.info("Preparing the messaging system");
                new AsynchronousAuthenticationServer(broker, accountCreationManager);
                LOGGER.info("Preparing the server...");
                SanityServer.test(config.getAuthenticationPort(), config.getAuthenticationHost());
                AuthenticationServer server = new AuthenticationServer(
                        config.getAuthenticationHost(),
                        config.getAuthenticationPort(),
                        manager,
                        accountCreationManager);
                LOGGER.info("Server open on " + server.getHost() + ":" + server.getPort());
                server.startServer();
                LOGGER.info("Server running");
                //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                //waitForInput(br);
            }
        } catch (Exception e) {
            LOGGER.error("An error occurred, closing the server...", e);
            LOGGER.info("Server closed.");
            System.exit(-1);
        }
        LOGGER.info("Server closed.");
    }

    private static void waitForInput(BufferedReader br) {

        String input = "";
        try {
            Terminal.print("Enter a command:");
            input = br.readLine();
            input = input == null ? "" : input;
        } catch (IOException e) {
            LOGGER.error("IO issue", e);
        }
        if(input.equalsIgnoreCase("EXIT") || input.equalsIgnoreCase("QUIT")) {
            System.exit(0);
        }
        waitForInput(br);
    }

}
