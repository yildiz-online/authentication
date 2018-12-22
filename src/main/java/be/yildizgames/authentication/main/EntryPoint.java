/*
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 *  Copyright (c) 2018 Grégory Van den Borre
 *
 *  More infos available: https://www.yildiz-games.be
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without
 *  limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 *  of the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 *  WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 *  OR COPYRIGHT  HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE  SOFTWARE.
 *
 */

package be.yildizgames.authentication.main;

import be.yildizgames.authentication.AccountCreationManager;
import be.yildizgames.authentication.AuthenticationManager;
import be.yildizgames.authentication.DataBaseAuthenticator;
import be.yildizgames.authentication.DatabaseAccountCreator;
import be.yildizgames.authentication.configuration.Configuration;
import be.yildizgames.authentication.network.AsynchronousAuthenticationServer;
import be.yildizgames.authentication.network.JavaMailEmailService;
import be.yildizgames.common.git.GitProperties;
import be.yildizgames.common.git.GitPropertiesProvider;
import be.yildizgames.common.logging.LogEngine;
import be.yildizgames.common.logging.LogEngineFactory;
import be.yildizgames.common.util.Util;
import be.yildizgames.module.database.DataBaseConnectionProvider;
import be.yildizgames.module.database.DatabaseConnectionProviderFactory;
import be.yildizgames.module.database.DatabaseUpdater;
import be.yildizgames.module.database.LiquibaseDatabaseUpdater;
import be.yildizgames.module.messaging.Broker;
import be.yildizgames.module.messaging.BrokerMessageDestination;
import be.yildizgames.module.messaging.JmsMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application entry point, contains the main method.
 *
 * @author Grégory Van den Borre
 */
public final class EntryPoint {


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
            Configuration config = Configuration.fromAppArgs(args);
            LogEngine logEngine = LogEngineFactory.getLogEngine();
            logEngine.configureFromProperties(config.getLoggerConfiguration());
            Logger logger = LoggerFactory.getLogger(EntryPoint.class);
            logger.info("Starting Authentication Server (PID:{})...", Util.getPid());
            GitProperties git = GitPropertiesProvider.getGitProperties();
            logger.info("Version: {}", git.getVersion());
            logger.info("Built at: {}", git.getBuildTime());

            logger.info("Preparing the database connection...");

            try(DataBaseConnectionProvider provider = DatabaseConnectionProviderFactory.getInstance().createWithHighPrivilege(config)) {
                provider.sanity();
                DatabaseUpdater databaseUpdater = LiquibaseDatabaseUpdater.fromConfigurationPath("authentication-database-update.xml");
                databaseUpdater.update(provider);
                Broker broker = Broker.getBroker(config);
                BrokerMessageDestination accountCreatedQueue = broker.registerQueue("authentication-creation");
                JmsMessageProducer producer = accountCreatedQueue.createProducer();
                AuthenticationManager manager = new AuthenticationManager(new DataBaseAuthenticator(provider));
                AccountCreationManager accountCreationManager =
                        new AccountCreationManager(new DatabaseAccountCreator(provider, producer), new JavaMailEmailService(config), config);
                logger.info("Preparing the messaging system");
                new AsynchronousAuthenticationServer(broker, accountCreationManager, manager);
                logger.info("Server running");
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(EntryPoint.class).error("An error occurred, closing the server...", e);
            LoggerFactory.getLogger(EntryPoint.class).info("Server closed.");
            System.exit(-1);
        }
    }



}
