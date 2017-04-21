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
import be.yildiz.common.log.Logger;
import be.yildiz.module.database.DataBaseConnectionProvider;
import be.yildiz.module.database.DatabaseConnectionProviderFactory;
import be.yildiz.module.network.server.SanityServer;

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
            Logger.setFile("/yildiz/authentication-server.log");
            Logger.setLevelDebug();
            Logger.debug("Debug logger level enabled.");

            Configuration config = Configuration.fromAppArgs(args);

            Logger.info("Preparing the database connection...");

            DataBaseConnectionProvider provider = new DatabaseConnectionProviderFactory().create(config);
            AuthenticationManager manager = new AuthenticationManager(new DataBaseAuthenticator(provider));
            provider.sanity();
            Logger.info("Database connection ready.");

            Logger.info("Preparing the server...");
            new SanityServer().test(config.getAuthenticationPort(), config.getAuthenticationHost());
            AuthenticationServer server = new AuthenticationServer(config.getAuthenticationHost(), config.getAuthenticationPort(), manager);
            Logger.info("Server open on " + server.getHost() + ":" + server.getPort());
            server.startServer();
        } catch (Exception e) {
            Logger.error("An error occurred, closing the server...");
            Logger.error(e);
            Logger.info("Server closed.");
            System.exit(-1);
        }

    }

}
