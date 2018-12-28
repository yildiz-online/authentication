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

package be.yildizgames.authentication.configuration;

import be.yildizgames.common.authentication.AuthenticationConfiguration;
import be.yildizgames.common.exception.initialization.InitializationException;
import be.yildizgames.common.file.FileProperties;
import be.yildizgames.common.logging.LoggerConfiguration;
import be.yildizgames.common.logging.LoggerPropertiesConfiguration;
import be.yildizgames.common.util.PropertiesHelper;
import be.yildizgames.module.database.DbProperties;
import be.yildizgames.module.database.SimpleDbProperties;
import be.yildizgames.module.messaging.BrokerProperties;

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

    private final DbProperties dbProperties;
    private final String brokerDataFolder;
    private final String brokerHost;
    private final int brokerPort;
    private final String emailLogin;
    private final String emailPassword;
    private final String emailTemplatePath;
    private final Properties properties;
    private final LoggerConfiguration loggerConfig;

    private Configuration(final Properties properties) {
        super();
        this.properties = properties;
        this.dbProperties = new SimpleDbProperties(properties);
        this.brokerDataFolder = PropertiesHelper.getValue(properties, "broker.data");
        this.brokerHost = PropertiesHelper.getValue(properties, "broker.host");
        this.brokerPort = PropertiesHelper.getIntValue(properties, "broker.port");
        this.emailLogin = PropertiesHelper.getValue(properties, "mail.login");
        this.emailPassword = PropertiesHelper.getValue(properties, "mail.password");
        this.emailTemplatePath = PropertiesHelper.getValue(properties, "mail.template.path");
        this.loggerConfig = LoggerPropertiesConfiguration.fromProperties(properties);
    }

    /**
     * Load a configuration from the application provided arguments.
     * @param args Array of arguments, 0 is expected to be the property file to use, other values can override the file content..
     * @return The build property file.
     */
    public static Configuration fromAppArgs(String[] args) {
        if (args == null || args.length == 0) {
            InitializationException.invalidConfigurationFile("Please pass the property file as an argument when starting application");
        }
        Properties properties = FileProperties.getPropertiesFromFile(Paths.get(args[0]), args);
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
    public final Path getEmailTemplatePath(String language) {
        return Paths.get(this.emailTemplatePath + File.separator + language);
    }

    public final Properties getProperties() {
        return properties;
    }

    public LoggerConfiguration getLoggerConfiguration() {
        return this.loggerConfig;
    }
}