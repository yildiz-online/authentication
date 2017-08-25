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

import be.yildiz.authentication.AuthenticationManager;
import be.yildiz.authentication.DataBaseAuthenticator;
import be.yildiz.authentication.configuration.Configuration;
import be.yildiz.authentication.network.AuthenticationServer;
import be.yildiz.module.database.DataBaseConnectionProvider;
import be.yildiz.module.database.DatabaseConnectionProviderFactory;
import be.yildiz.module.database.DatabaseUpdater;
import be.yildiz.module.database.LiquibaseDatabaseUpdater;
import be.yildiz.module.network.server.SanityServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            DataBaseConnectionProvider provider = new DatabaseConnectionProviderFactory().create(config);
            provider.sanity();
            LOGGER.info("Database connection ready.");
            LOGGER.info("Updating database schema...");
            DatabaseUpdater databaseUpdater = new LiquibaseDatabaseUpdater("authentication-database-update.xml");
            databaseUpdater.update(provider);
            LOGGER.info("Database schema up to date.");
            AuthenticationManager manager = new AuthenticationManager(new DataBaseAuthenticator(provider));

            LOGGER.info("Preparing the server...");
            new SanityServer().test(config.getAuthenticationPort(), config.getAuthenticationHost());
            AuthenticationServer server = new AuthenticationServer(config.getAuthenticationHost(), config.getAuthenticationPort(), manager);
            LOGGER.info("Server open on " + server.getHost() + ":" + server.getPort());
            server.startServer();
        } catch (Exception e) {
            LOGGER.error("An error occurred, closing the server...", e);
            LOGGER.info("Server closed.");
            System.exit(-1);
        }

    }

}
