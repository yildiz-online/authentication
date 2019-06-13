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

package be.yildizgames.authentication.configuration;

import java.util.Properties;

/**
 * @author Grégory Van den Borre
 */
public class DefaultConfigProperties extends Properties {

    private static final String ROOT = System.getProperty("user.home") + "/yildiz-authentication";

    private DefaultConfigProperties() {
        super();
        this.setProperty("database.host", "localhost");
        this.setProperty("database.port", "9000");
        this.setProperty("database.name", "authentication");
        this.setProperty("database.user", "sa");
        this.setProperty("database.password", "");
        this.setProperty("database.root.user", "sa");
        this.setProperty("database.root.password", "");
        this.setProperty("database.system", "derby-file");
        this.setProperty("mail.login", "user");
        this.setProperty("mail.password", "user");
        this.setProperty("mail.template.path", "user");
        this.setProperty("broker.host", "user");
        this.setProperty("broker.port", "1");
        this.setProperty("broker.data", ROOT + "/data/broker");
        this.setProperty("broker.internal", "true");
        this.setProperty("logger.pattern", "%d{HH:mm:ss.SSS} | %level | %class | %msg%n");
        this.setProperty("logger.configuration.file", ROOT + "/config/logback.xml");
        this.setProperty("logger.tcp.host", "localhost");
        this.setProperty("logger.tcp.port", "60000");
        this.setProperty("logger.level", "INFO");
        this.setProperty("logger.output", "CONSOLE");
        this.setProperty("logger.file.output", ROOT + "/logs/log.log");
        this.setProperty("logger.disabled", "");
    }

    public static Properties create() {
        return new DefaultConfigProperties();
    }
}
