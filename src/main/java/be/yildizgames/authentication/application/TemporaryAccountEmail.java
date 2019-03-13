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

package be.yildizgames.authentication.application;

import be.yildizgames.authentication.infrastructure.io.mail.EmailException;
import be.yildizgames.authentication.infrastructure.io.mail.EmailTemplate;
import be.yildizgames.common.exception.implementation.ImplementationException;
import be.yildizgames.common.util.StringUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Grégory Van den Borre
 */
class TemporaryAccountEmail implements EmailTemplate {

    /**
     * Email title, in the user language.
     */
    private final String title;

    /**
     * Email body, in the user language.
     */
    private final String body;

    /**
     * Email address.
     */
    private final String email;

    /**
     * Create a new instance.
     * @param emailTemplate Path to the email template file.
     * @param login Email receiver name.
     * @param email Receiver email address.
     * @param token Unique token to validate the account.
     */
    TemporaryAccountEmail(final Path emailTemplate, final String login, final String email, final String token) {
        ImplementationException.throwForNull(emailTemplate);
        ImplementationException.throwForNull(login);
        ImplementationException.throwForNull(email);
        ImplementationException.throwForNull(token);
        this.email = email;
        try {
            String content = Files.readString(emailTemplate, StandardCharsets.UTF_8);
            String[] values = content.split("##");
            if (values.length < 2) {
                throw new EmailException("Invalid content, '##' expected between the title and the body");
            }
            this.title = values[0];
            String[] params = {login, email, token};
            this.body = StringUtil.fillVariable(values[1], params);
        } catch (IOException e) {
            throw new EmailException(e);
        }
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getBody() {
        return this.body;
    }

    @Override
    public String getEmail() {
        return this.email;
    }
}
