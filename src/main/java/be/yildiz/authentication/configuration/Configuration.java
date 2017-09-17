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

package be.yildiz.authentication.configuration;

import be.yildiz.common.exeption.InitializationException;
import be.yildiz.common.resource.PropertiesHelper;
import be.yildiz.module.database.DbProperties;
import be.yildiz.module.network.AuthenticationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

/**
 * Load and contains the configuration data to start the application.
 * This class  will:
 * Load the properties from a file.
 * Check if every expected keys are present.
 * Check if every values are provided and in the correct range.
 * Expose all the values through getters.
 * If the file cannot be loaded of if the keys are not all available, an exception is thrown.
 * @author Grégory Van den Borre
 */
public class Configuration implements DbProperties, AuthenticationConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    /**
     * Login to connect the database
     */
    private final String dbUser;

    /**
     * Password to connect to the database
     */
    private final String dbPassword;

    /**
     * Database to use.
     */
    private final String dbName;

    /**
     * Host address to connect to the database.
     */
    private final String dbHost;

    /**
     * Port to connect to the database.
     */
    private final int dbPort;

    /**
     * Database system to use.
     */
    private final String system;

    /**
     * Port to expose.
     */
    private final int port;

    /**
     * Address to use for connection.
     */
    private final String address;
    private final String dbRootUser;
    private final String dbRootPassword;

    private Configuration(final Properties properties) {
        super();
        this.dbUser = PropertiesHelper.getValue(properties,"database.user");
        this.dbPassword = PropertiesHelper.getValue(properties,"database.password");
        this.dbRootUser = PropertiesHelper.getValue(properties,"database.root.user");
        this.dbRootPassword = PropertiesHelper.getValue(properties,"database.root.password");
        this.dbName = PropertiesHelper.getValue(properties,"database.name");
        this.dbHost = PropertiesHelper.getValue(properties, "database.host");
        this.dbPort = PropertiesHelper.getIntValue(properties, "database.port");
        this.system = PropertiesHelper.getValue(properties,"database.system");
        DbPropertiesInvariant.check(this.dbUser, this.dbPassword, this.dbRootUser, this.dbRootPassword, this.dbName, this.dbHost, this.dbPort, this.system);
        this.port = PropertiesHelper.getIntValue(properties, "network.port");
        this.address = PropertiesHelper.getValue(properties,"network.host");
        AuthenticationConfigurationInvariant.check(this.address, this.port);
        LOGGER.info("Property file loaded.");
    }

    /**
     * Load a configuration from the application provided arguments.
     * @param args Array of arguments, 0 is expected to be the property file to use, other values can override the file content..
     * @return The build property file.
     */
    public static Configuration fromAppArgs(String[] args) {
        if (args == null || args.length == 0) {
            throw new InitializationException("Please pass the property file as an argument when starting application");
        }
        LOGGER.info("Reading property file...");
        Properties properties = PropertiesHelper.getPropertiesFromFile(new File(args[0]), args);
        return new Configuration(properties);
    }

    @Override
    public final String getDbUser() {
        return this.dbUser;
    }

    @Override
    public final int getDbPort() {
        return this.dbPort;
    }

    @Override
    public final String getDbPassword() {
        return this.dbPassword;
    }

    @Override
    public final String getDbHost() {
        return this.dbHost;
    }

    @Override
    public final String getDbName() {
        return this.dbName;
    }

    @Override
    public final String getAuthenticationHost() {
        return this.address;
    }

    @Override
    public final int getAuthenticationPort() {
        return this.port;
    }

    @Override
    public final String getSystem() {
        return this.system;
    }

    @Override
    public String getDbRootUser() {
        return this.dbRootUser;
    }

    @Override
    public String getDbRootPassword() {
        return this.dbRootPassword;
    }
}
