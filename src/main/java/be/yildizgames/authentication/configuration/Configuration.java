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

package be.yildizgames.authentication.configuration;

import be.yildizgames.authentication.infrastructure.io.mail.EmailProperties;
import be.yildizgames.authentication.infrastructure.io.mail.EmailPropertiesStandard;
import be.yildizgames.common.authentication.AuthenticationConfiguration;
import be.yildizgames.common.logging.LoggerConfiguration;
import be.yildizgames.common.logging.LoggerPropertiesConfiguration;
import be.yildizgames.module.database.DbProperties;
import be.yildizgames.module.database.DbPropertiesStandard;
import be.yildizgames.module.messaging.BrokerProperties;
import be.yildizgames.module.messaging.BrokerPropertiesStandard;

import java.nio.file.Path;
import java.util.Objects;
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
public class Configuration implements DbProperties, AuthenticationConfiguration, BrokerProperties, EmailProperties {

    private final DbProperties dbProperties;

    private final EmailProperties emailProperties;

    private final Properties properties;

    private final LoggerConfiguration loggerConfig;

    private final BrokerProperties brokerProperties;

    public Configuration(final Properties properties) {
        super();
        Objects.requireNonNull(properties);
        this.properties = properties;
        this.dbProperties = DbPropertiesStandard.fromProperties(properties);
        this.brokerProperties = BrokerPropertiesStandard.fromProperties(properties);
        this.emailProperties = EmailPropertiesStandard.fromProperties(properties);
        this.loggerConfig = LoggerPropertiesConfiguration.fromProperties(properties);
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
        return this.brokerProperties.getBrokerDataFolder();
    }

    @Override
    public boolean getBrokerInternal() {
        return this.brokerProperties.getBrokerInternal();
    }

    @Override
    public final String getBrokerHost() {
        return this.brokerProperties.getBrokerHost();
    }

    @Override
    public final int getBrokerPort() {
        return this.brokerProperties.getBrokerPort();
    }

    @Override
    public final String getEmailLogin() {
        return this.emailProperties.getEmailLogin();
    }

    @Override
    public final String getEmailPassword() {
        return this.emailProperties.getEmailPassword();
    }

    @Override
    public final Path getEmailTemplatePath(String language) {
        return this.emailProperties.getEmailTemplatePath(language);
    }

    public final Properties getProperties() {
        return properties;
    }

    public LoggerConfiguration getLoggerConfiguration() {
        return this.loggerConfig;
    }
}
