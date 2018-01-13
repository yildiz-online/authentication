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
import be.yildiz.authentication.network.JavaMailEmailService;
import be.yildiz.module.database.DataBaseConnectionProvider;
import be.yildiz.module.database.DatabaseConnectionProviderFactory;
import be.yildiz.module.database.DatabaseUpdater;
import be.yildiz.module.database.LiquibaseDatabaseUpdater;
import be.yildiz.module.messaging.ActivemqBroker;
import be.yildiz.module.messaging.Broker;
import be.yildiz.module.messaging.BrokerMessageDestination;
import be.yildiz.module.messaging.JmsMessageProducer;
import be.yildizgames.common.logging.LogFactory;
import be.yildizgames.module.database.postgresql.PostgresqlSystem;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.Properties;

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
        Logger logger = LogFactory.getInstance().getLogger(EntryPoint.class);
        logger.info("Authentication Server.");
        try(InputStream is = EntryPoint.class.getClassLoader().getResourceAsStream("git.properties")) {
            Properties p = new Properties();
            p.load(is);
            GitProperties git = new GitProperties(p);
            logger.info("Version:" + git.getVersion());
            logger.info("Built at:" + git.getBuildTime());
        } catch (Exception e) {
            logger.error("Could not retrieve git info:", e);
        }
        try {
            logger.debug("Debug logger level enabled.");
            Configuration config = Configuration.fromAppArgs(args);

            logger.info("Preparing the database connection...");
            PostgresqlSystem.support();
            try(DataBaseConnectionProvider provider = DatabaseConnectionProviderFactory.getInstance().createWithHighPrivilege(config)) {
                provider.sanity();
                DatabaseUpdater databaseUpdater = LiquibaseDatabaseUpdater.fromConfigurationPath("authentication-database-update.xml");
                databaseUpdater.update(provider);
                Broker broker = ActivemqBroker.initialize(config);
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
            logger.error("An error occurred, closing the server...", e);
            logger.info("Server closed.");
            System.exit(-1);
        }
    }

}
