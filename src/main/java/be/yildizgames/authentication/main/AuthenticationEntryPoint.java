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
import be.yildizgames.authentication.infrastructure.persistence.DataBaseAuthenticator;
import be.yildizgames.authentication.infrastructure.persistence.DatabaseAccountCreator;
import be.yildizgames.authentication.configuration.Configuration;
import be.yildizgames.authentication.infrastructure.AsynchronousAuthenticationServer;
import be.yildizgames.authentication.infrastructure.io.mail.JavaMailEmailService;
import be.yildizgames.common.application.Starter;
import be.yildizgames.module.database.DataBaseConnectionProvider;
import be.yildizgames.module.database.DatabaseConnectionProviderFactory;
import be.yildizgames.module.database.DatabaseUpdater;
import be.yildizgames.module.database.LiquibaseDatabaseUpdater;
import be.yildizgames.module.messaging.Broker;
import be.yildizgames.module.messaging.BrokerMessageDestination;
import be.yildizgames.module.messaging.BrokerMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application entry point, contains the main method.
 *
 * @author Grégory Van den Borre
 */
final class AuthenticationEntryPoint {

    private AuthenticationEntryPoint() {
        super();
    }

    static AuthenticationEntryPoint create() {
        return new AuthenticationEntryPoint();
    }

    void start(Configuration config) {
        try {
            //TODO have all entry point impl an interface, pass that interface to starter, and let starter launch it.
            Starter.start(config.getLoggerConfiguration(), "Authentication Server");
            Logger logger = LoggerFactory.getLogger(AuthenticationEntryPoint.class);

            logger.info("Preparing the database connection...");

            try(DataBaseConnectionProvider provider = DatabaseConnectionProviderFactory.getInstance().createWithHighPrivilege(config)) {
                provider.sanity();
                DatabaseUpdater databaseUpdater = LiquibaseDatabaseUpdater.fromConfigurationPath("authentication-database-update.xml");
                databaseUpdater.update(provider);
                Broker broker = Broker.getBroker(config);
                BrokerMessageDestination accountCreatedQueue = broker.registerQueue("authentication-creation");
                BrokerMessageProducer producer = accountCreatedQueue.createProducer();
                AuthenticationManager manager = new AuthenticationManager(new DataBaseAuthenticator(provider));
                AccountCreationManager accountCreationManager =
                        new AccountCreationManager(new DatabaseAccountCreator(provider, producer), new JavaMailEmailService(config), config);
                logger.info("Preparing the messaging system");
                new AsynchronousAuthenticationServer(broker, accountCreationManager, manager);
                logger.info("Server running");
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(AuthenticationEntryPoint.class).error("An error occurred, closing the server...", e);
            LoggerFactory.getLogger(AuthenticationEntryPoint.class).info("Server closed.");
            System.exit(-1);
        }
    }

}