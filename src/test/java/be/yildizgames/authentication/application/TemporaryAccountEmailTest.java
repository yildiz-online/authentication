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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class TemporaryAccountEmailTest {

    @Test
    public void happyFlow() throws URISyntaxException {
        TemporaryAccountEmail email = new TemporaryAccountEmail(Paths.get(ClassLoader.getSystemResource("mail.txt").toURI()), "testl", "testm", "testt");
        Assertions.assertEquals("Yildiz-Online account confirmation", email.getTitle());
        Assertions.assertEquals("Dear testl, Please activate your account by clicking on the following link:\\n\\nhttps:\\\\www.yildiz-games.be/api/v1/accounts/confirmation?email=testm&token=testt", email.getBody());
        Assertions.assertEquals("testm", email.getEmail());
    }

    @Test
    public void invalidTemplateFile() {
        Assertions.assertThrows(EmailException.class, () -> new TemporaryAccountEmail(Paths.get(ClassLoader.getSystemResource("mail-invalid.txt").toURI()), "testl", "testm", "testt"));
    }

    @Test
    public void notExistingTemplateFile() {
        Assertions.assertThrows(EmailException.class, () -> new TemporaryAccountEmail(Paths.get("notExists"), "testl", "testm", "testt"));
    }

    @Test
    public void ioExceptionTemplateFile() {
        Exception e = Assertions.assertThrows(EmailException.class, () -> new TemporaryAccountEmail(Paths.get(ClassLoader.getSystemResource("dir").toURI()), "testl", "testm", "testt"));
        Assertions.assertTrue(e.getCause() instanceof IOException);
    }

}
