/*
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 *  Copyright (c) 2019 Grégory Van den Borre
 *
 *  More infos available: https://engine.yildiz-games.be
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

import be.yildizgames.authentication.application.AccountCreationManager;
import be.yildizgames.authentication.application.AuthenticationManager;
import be.yildizgames.authentication.configuration.Configuration;
import be.yildizgames.authentication.configuration.DefaultConfigProperties;
import be.yildizgames.authentication.infrastructure.AsynchronousAuthenticationServer;
import be.yildizgames.authentication.infrastructure.io.mail.JavaMailEmailService;
import be.yildizgames.authentication.infrastructure.persistence.DataBaseAuthenticator;
import be.yildizgames.authentication.infrastructure.persistence.DatabaseAccountCreator;
import be.yildizgames.common.application.Application;
import be.yildizgames.common.authentication.protocol.Queues;
import be.yildizgames.common.logging.Logger;
import be.yildizgames.module.database.DataBaseConnectionProvider;
import be.yildizgames.module.database.DatabaseConnectionProviderFactory;
import be.yildizgames.module.database.derby.DerbySystem;
import be.yildizgames.module.database.updater.DatabaseUpdater;
import be.yildizgames.module.database.updater.LiquibaseDatabaseUpdater;
import be.yildizgames.module.messaging.Broker;
import be.yildizgames.module.messaging.BrokerMessageDestination;
import be.yildizgames.module.messaging.BrokerMessageProducer;

/**
 * Application entry point, contains the main method.
 *
 * @author Grégory Van den Borre
 */
public final class AuthenticationEntryPoint {

    /**
     * Private constructor to prevent instantiation, use create method instead.
     */
    private AuthenticationEntryPoint() {
        super();
    }

    public static AuthenticationEntryPoint create() {
        return new AuthenticationEntryPoint();
    }

    public void start(String[] args) {
        Logger logger = Logger.getLogger(AuthenticationEntryPoint.class);
        try {
            Application application = Application
                    .prepare("Authentication Server")
                    .withConfiguration(args, DefaultConfigProperties.create(), DerbySystem::support)
                    .start();

            Configuration config = new Configuration(application.getConfiguration());



            logger.info("Preparing the database connection...");

            try(DataBaseConnectionProvider provider = DatabaseConnectionProviderFactory.getInstance().createWithHighPrivilege(config)) {
                provider.sanity();
                DatabaseUpdater databaseUpdater = LiquibaseDatabaseUpdater.fromConfigurationPath("authentication-database-update.xml");
                databaseUpdater.update(provider);
                Broker broker = Broker.getBroker(config);
                BrokerMessageDestination accountCreatedQueue = broker.registerQueue(Queues.CREATE_ACCOUNT_CONFIRMATION_RESPONSE.getName());
                BrokerMessageProducer producer = accountCreatedQueue.createProducer();
                AuthenticationManager manager = new AuthenticationManager(new DataBaseAuthenticator(provider));
                AccountCreationManager accountCreationManager =
                        new AccountCreationManager(new DatabaseAccountCreator(provider, producer), new JavaMailEmailService(config), config);
                logger.info("Preparing the messaging system");
                new AsynchronousAuthenticationServer(broker, accountCreationManager, manager);
                logger.info("Server running");
            }
        } catch (Exception e) {
            logger.error(e);
            logger.info("Server closed.");
            System.exit(-1);
        }
    }

}
