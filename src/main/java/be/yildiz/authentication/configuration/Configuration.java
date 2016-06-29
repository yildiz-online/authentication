//        This file is part of the Yildiz-Online project, licenced under the MIT License
//        (MIT)
//
//        Copyright (c) 2016 Grégory Van den Borre
//
//        More infos available: http://yildiz.bitbucket.org
//
//        Permission is hereby granted, free of charge, to any person obtaining a copy
//        of this software and associated documentation files (the "Software"), to deal
//        in the Software without restriction, including without limitation the rights
//        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//        copies of the Software, and to permit persons to whom the Software is
//        furnished to do so, subject to the following conditions:
//
//        The above copyright notice and this permission notice shall be included in all
//        copies or substantial portions of the Software.
//
//        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//        SOFTWARE.

package be.yildiz.authentication.configuration;

import be.yildiz.common.resource.PropertiesHelper;
import be.yildiz.module.database.DbProperties;
import be.yildiz.module.network.AuthenticationConfiguration;

import java.util.Properties;

/**
 * @author Grégory Van den Borre
 */
public class Configuration implements DbProperties, AuthenticationConfiguration {


    private final String dbUser;
    private final String dbPassword;
    private final String dbName;
    private final String dbHost;
    private final int dbPort;
    private final int port;
    private final String address;

    public Configuration(Properties properties) {
        super();
        this.dbUser = properties.getProperty("database.user");
        this.dbPassword = properties.getProperty("database.password");
        this.dbName = properties.getProperty("database.name");
        this.dbHost = properties.getProperty("database.host");
        this.dbPort = PropertiesHelper.getIntValue(properties, "database.port");
        this.port = PropertiesHelper.getIntValue(properties, "network.port");
        this.address = properties.getProperty("network.host");
    }

    @Override
    public String getDbUser() {
        return this.dbUser;
    }

    @Override
    public int getDbPort() {
        return this.dbPort;
    }

    @Override
    public String getDbPassword() {
        return this.dbPassword;
    }

    @Override
    public String getDbHost() {
        return this.dbHost;
    }

    @Override
    public String getDbName() {
        return this.dbName;
    }

    @Override
    public String getAuthenticationHost() {
        return this.address;
    }

    @Override
    public int getAuthenticationPort() {
        return this.port;
    }
}
