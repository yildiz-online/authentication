/*
 *
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 * Copyright (c) 2019 Grégory Van den Borre
 *
 * More infos available: https://engine.yildiz-games.be
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 *  WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT  HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE  SOFTWARE.
 *
 *
 */

package be.yildizgames.authentication.main;

import be.yildizgames.authentication.configuration.Configuration;
import be.yildizgames.authentication.configuration.DefaultConfigProperties;
import be.yildizgames.common.configuration.ConfigurationNotFoundException;
import be.yildizgames.common.configuration.ConfigurationRetrieverFactory;
import be.yildizgames.common.configuration.parameter.ApplicationArgs;
import be.yildizgames.common.exception.initialization.InitializationException;
import be.yildizgames.common.logging.LogEngineProvider;
import be.yildizgames.common.logging.PreLogger;
import be.yildizgames.module.database.derby.DerbySystem;

import java.util.Properties;

public class StandardEntryPoint {

    private static final PreLogger PRELOGGER = LogEngineProvider.getLoggerProvider().getLogEngine().getPrelogger();


    private StandardEntryPoint() {
        super();
    }

    /**
     * Register a new log file, the connection properties to the database from a property file, launch the 2 network servers, authentication server, and token validation server, and start listening
     * for client requests.
     *
     * @param args Unused.
     */
    public static void main(String[] args) {
        Configuration config = Configuration.fromProperties(getProperties(args));
        AuthenticationEntryPoint
                .create()
                .start(config);
    }

    private static Properties getProperties(String[] args) {
        PRELOGGER.info("Loading properties.");
        try {
            return ConfigurationRetrieverFactory
                    .fromFile(new ConfigurationNotFoundException())
                    .retrieveFromArgs(ApplicationArgs.of(args));
        } catch (InitializationException e) {
            PRELOGGER.info("Loading properties failed, fallback to default values.");
            DerbySystem.support();
            return new DefaultConfigProperties();
        }
    }
}
