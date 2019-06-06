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

package be.yildizgames.authentication.infrastructure.io.mail;

import be.yildizgames.common.configuration.PropertiesHelper;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Grégory Van den Borre
 */
public class EmailPropertiesStandard implements EmailProperties {

    private final String login;

    private final String password;

    private final String templatePath;

    private final Properties properties;

    private EmailPropertiesStandard(final Properties properties) {
        super();
        Objects.requireNonNull(properties);
        this.properties = properties;
        this.login = PropertiesHelper.getValue(properties, "mail.login");
        this.password = PropertiesHelper.getValue(properties, "mail.password");
        this.templatePath = PropertiesHelper.getValue(properties, "mail.template.path");
    }

    public static EmailProperties fromProperties(Properties properties) {
        return new EmailPropertiesStandard(properties);
    }

    @Override
    public final String getEmailLogin() {
        return this.login;
    }

    @Override
    public final String getEmailPassword() {
        return this.password;
    }

    @Override
    public final Properties getProperties() {
        return this.properties;
    }

    @Override
    public final Path getEmailTemplatePath(String language) {
        return Paths.get(this.templatePath + File.separator + language);
    }
}
