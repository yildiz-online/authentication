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

import be.yildiz.module.database.DbProperties;
import be.yildiz.module.database.SimpleDbProperties;
import be.yildiz.module.messaging.BrokerProperties;
import be.yildiz.module.network.AuthenticationConfiguration;
import be.yildizgames.common.exception.technical.InitializationException;
import be.yildizgames.common.file.FileProperties;
import be.yildizgames.common.util.PropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class Configuration implements DbProperties, AuthenticationConfiguration, BrokerProperties, EmailTemplateConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);


    /**
     * Port to expose.
     */
    private final int port;

    /**
     * Address to use for connection.
     */
    private final String address;
    private final DbProperties dbProperties;
    private final String brokerDataFolder;
    private final String brokerHost;
    private final int brokerPort;
    private final String emailLogin;
    private final String emailPassword;
    private final Path emailTemplatePath;
    private final Properties properties;

    private Configuration(final Properties properties) {
        super();
        this.properties = properties;
        this.dbProperties = new SimpleDbProperties(properties);
        this.port = PropertiesHelper.getIntValue(properties, "network.port");
        this.address = PropertiesHelper.getValue(properties,"network.host");
        this.brokerDataFolder = PropertiesHelper.getValue(properties, "broker.data");
        this.brokerHost = PropertiesHelper.getValue(properties, "broker.host");
        this.brokerPort = PropertiesHelper.getIntValue(properties, "broker.port");
        this.emailLogin = PropertiesHelper.getValue(properties, "mail.login");
        this.emailPassword = PropertiesHelper.getValue(properties, "mail.password");
        this.emailTemplatePath = Paths.get(PropertiesHelper.getValue(properties, "mail.template.path"));
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
        Properties properties = FileProperties.getPropertiesFromFile(new File(args[0]), args);
        return new Configuration(properties);
    }

    @Override
    public final String getDbUser() {
        return this.dbProperties.getDbUser();
    }

    @Override
    public final int getDbPort() {
        return this.dbProperties.getDbPort();
    }

    @Override
    public final String getDbPassword() {
        return this.dbProperties.getDbPassword();
    }

    @Override
    public final String getDbHost() {
        return this.dbProperties.getDbHost();
    }

    @Override
    public final String getDbName() {
        return this.dbProperties.getDbName();
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
        return this.dbProperties.getSystem();
    }

    @Override
    public final String getDbRootUser() {
        return this.dbProperties.getDbRootUser();
    }

    @Override
    public final String getDbRootPassword() {
        return this.dbProperties.getDbRootPassword();
    }

    @Override
    public final String getBrokerDataFolder() {
        return this.brokerDataFolder;
    }

    @Override
    public final String getBrokerHost() {
        return this.brokerHost;
    }

    @Override
    public final int getBrokerPort() {
        return this.brokerPort;
    }

    public final String getEmailLogin() {
        return this.emailLogin;
    }

    public final String getEmailPassword() {
        return this.emailPassword;
    }

    @Override
    public final Path getEmailTemplatePath() {
        return this.emailTemplatePath;
    }

    public final Properties getProperties() {
        return properties;
    }
}
