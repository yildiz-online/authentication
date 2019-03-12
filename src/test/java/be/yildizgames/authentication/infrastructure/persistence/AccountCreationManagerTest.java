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

package be.yildizgames.authentication.infrastructure.persistence;

import be.yildizgames.authentication.application.AccountCreationManager;
import be.yildizgames.authentication.application.AccountCreator;
import be.yildizgames.authentication.infrastructure.TemporaryAccountDto;
import be.yildizgames.common.authentication.protocol.AccountConfirmationDto;
import be.yildizgames.common.authentication.protocol.TemporaryAccountCreationResultDto;
import be.yildizgames.common.exception.implementation.ImplementationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * @author Grégory Van den Borre
 */
class AccountCreationManagerTest {

    @Nested
    class Constructor {

        @Test
        void happyFlow() {
            AccountCreationManager acm = new AccountCreationManager(givenAnAccountCreator(true, true), (m) -> {}, (l) -> Paths.get(""));
            Assertions.assertNotNull(acm);
        }

        @Test
        void withNullAccountCreator() {
            Assertions.assertThrows(ImplementationException.class, () -> new AccountCreationManager(null, (m) -> {}, (l) -> Paths.get("")));
        }

        @Test
        void withNullEmailService() {
            Assertions.assertThrows(ImplementationException.class, () -> new AccountCreationManager(givenAnAccountCreator(true, true), null, (l) -> Paths.get("")));
        }

        @Test
        void withNullEmailConfiguration() {
            Assertions.assertThrows(ImplementationException.class, () -> new AccountCreationManager(givenAnAccountCreator(true, true), (m) -> {}, null));
        }

    }

    @Nested
    class Create {

        @Test
        void happyFlow() {
            AccountCreationManager m = givenAManager(givenAnAccountCreator(false, false));
            TemporaryAccountCreationResultDto dto = m.create(new TemporaryAccountDto("loginok", "passwordok", "me@me.com", "en"));
            Assertions.assertFalse(dto.hasError());
            Assertions.assertFalse(dto.isInvalidPassword());
            Assertions.assertFalse(dto.isInvalidEmail());
            Assertions.assertFalse(dto.isInvalidLogin());
            Assertions.assertFalse(dto.isEmailMissing());
            Assertions.assertFalse(dto.isAccountExisting());
            Assertions.assertFalse(dto.isEmailExisting());
            Assertions.assertFalse(dto.isTechnicalIssue());
        }

        @Test
        void loginEmpty() {
            AccountCreationManager m = givenAManager(givenAnAccountCreator(false, false));
            TemporaryAccountCreationResultDto dto = m.create(new TemporaryAccountDto("", "passwordok", "me@me.com", "en"));
            Assertions.assertTrue(dto.isInvalidLogin());
            Assertions.assertFalse(dto.isInvalidPassword());
            Assertions.assertFalse(dto.isInvalidEmail());
            Assertions.assertFalse(dto.isEmailMissing());
            Assertions.assertFalse(dto.isAccountExisting());
            Assertions.assertFalse(dto.isEmailExisting());
            Assertions.assertFalse(dto.isTechnicalIssue());
            Assertions.assertTrue(dto.hasError());
        }

        @Test
        void loginTooShort() {
            AccountCreationManager m = givenAManager(givenAnAccountCreator(false, false));
            TemporaryAccountCreationResultDto dto = m.create(new TemporaryAccountDto("a", "passwordok", "me@me.com", "en"));
            Assertions.assertTrue(dto.isInvalidLogin());
            Assertions.assertFalse(dto.isInvalidPassword());
            Assertions.assertFalse(dto.isInvalidEmail());
            Assertions.assertFalse(dto.isEmailMissing());
            Assertions.assertFalse(dto.isAccountExisting());
            Assertions.assertFalse(dto.isEmailExisting());
            Assertions.assertFalse(dto.isTechnicalIssue());
            Assertions.assertTrue(dto.hasError());
        }

        @Test
        void loginTooLong() {
            AccountCreationManager m = givenAManager(givenAnAccountCreator(false, false));
            TemporaryAccountCreationResultDto dto = m.create(new TemporaryAccountDto("123456789012345678901234567890", "passwordok", "me@me.com", "en"));
            Assertions.assertTrue(dto.isInvalidLogin());
            Assertions.assertFalse(dto.isInvalidPassword());
            Assertions.assertFalse(dto.isInvalidEmail());
            Assertions.assertFalse(dto.isEmailMissing());
            Assertions.assertFalse(dto.isAccountExisting());
            Assertions.assertFalse(dto.isEmailExisting());
            Assertions.assertFalse(dto.isTechnicalIssue());
            Assertions.assertTrue(dto.hasError());
        }

        @Test
        void loginInvalid() {
            AccountCreationManager m = givenAManager(givenAnAccountCreator(false, false));
            TemporaryAccountCreationResultDto dto = m.create(new TemporaryAccountDto("|&é@-{}", "passwordok", "me@me.com", "en"));
            Assertions.assertTrue(dto.isInvalidLogin());
            Assertions.assertFalse(dto.isInvalidPassword());
            Assertions.assertFalse(dto.isInvalidEmail());
            Assertions.assertFalse(dto.isEmailMissing());
            Assertions.assertFalse(dto.isAccountExisting());
            Assertions.assertFalse(dto.isEmailExisting());
            Assertions.assertFalse(dto.isTechnicalIssue());
            Assertions.assertTrue(dto.hasError());
        }

        @Test
        void loginExisting() {
            AccountCreationManager m = givenAManager(givenAnAccountCreator(true, false));
            TemporaryAccountCreationResultDto dto = m.create(new TemporaryAccountDto("login", "passwordok", "me@me.com", "en"));
            Assertions.assertTrue(dto.isAccountExisting());
            Assertions.assertFalse(dto.isInvalidPassword());
            Assertions.assertFalse(dto.isInvalidEmail());
            Assertions.assertFalse(dto.isInvalidLogin());
            Assertions.assertFalse(dto.isEmailMissing());
            Assertions.assertFalse(dto.isEmailExisting());
            Assertions.assertFalse(dto.isTechnicalIssue());
            Assertions.assertTrue(dto.hasError());
        }

        @Test
        void passwordEmpty() {
            AccountCreationManager m = givenAManager(givenAnAccountCreator(false, false));
            TemporaryAccountCreationResultDto dto = m.create(new TemporaryAccountDto("loginok", "", "me@me.com", "en"));
            Assertions.assertTrue(dto.isInvalidPassword());
            Assertions.assertFalse(dto.isInvalidEmail());
            Assertions.assertFalse(dto.isInvalidLogin());
            Assertions.assertFalse(dto.isEmailMissing());
            Assertions.assertFalse(dto.isAccountExisting());
            Assertions.assertFalse(dto.isEmailExisting());
            Assertions.assertFalse(dto.isTechnicalIssue());
            Assertions.assertTrue(dto.hasError());
        }

        @Test
        void passwordTooShort() {
            AccountCreationManager m = givenAManager(givenAnAccountCreator(false, false));
            TemporaryAccountCreationResultDto dto = m.create(new TemporaryAccountDto("loginok", "p", "me@me.com", "en"));
            Assertions.assertTrue(dto.isInvalidPassword());
            Assertions.assertFalse(dto.isInvalidEmail());
            Assertions.assertFalse(dto.isInvalidLogin());
            Assertions.assertFalse(dto.isEmailMissing());
            Assertions.assertFalse(dto.isAccountExisting());
            Assertions.assertFalse(dto.isEmailExisting());
            Assertions.assertFalse(dto.isTechnicalIssue());
            Assertions.assertTrue(dto.hasError());
        }

        @Test
        void passwordTooLong() {
            AccountCreationManager m = givenAManager(givenAnAccountCreator(false, false));
            TemporaryAccountCreationResultDto dto = m.create(new TemporaryAccountDto("loginok", "paaaaaaaaaaaaaaaaaaaaaaaaaaaazzzzzzzzzzzzzzzzzzzzzzzzzzzzeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee", "me@me.com", "en"));
            Assertions.assertTrue(dto.isInvalidPassword());
            Assertions.assertFalse(dto.isInvalidEmail());
            Assertions.assertFalse(dto.isInvalidLogin());
            Assertions.assertFalse(dto.isEmailMissing());
            Assertions.assertFalse(dto.isAccountExisting());
            Assertions.assertFalse(dto.isEmailExisting());
            Assertions.assertFalse(dto.isTechnicalIssue());
            Assertions.assertTrue(dto.hasError());
        }

        @Test
        void passwordInvalid() {
            AccountCreationManager m = givenAManager(givenAnAccountCreator(false, false));
            TemporaryAccountCreationResultDto dto = m.create(new TemporaryAccountDto("loginok", "&é'(§§è", "me@me.com", "en"));
            Assertions.assertTrue(dto.isInvalidPassword());
            Assertions.assertFalse(dto.isInvalidEmail());
            Assertions.assertFalse(dto.isInvalidLogin());
            Assertions.assertFalse(dto.isEmailMissing());
            Assertions.assertFalse(dto.isAccountExisting());
            Assertions.assertFalse(dto.isEmailExisting());
            Assertions.assertFalse(dto.isTechnicalIssue());
            Assertions.assertTrue(dto.hasError());
        }

        @Test
        void emailInvalid() {
            AccountCreationManager m = givenAManager(givenAnAccountCreator(false, false));
            TemporaryAccountCreationResultDto dto = m.create(new TemporaryAccountDto("", "passwordok", "bademail", "en"));
            Assertions.assertTrue(dto.isInvalidEmail());
            Assertions.assertTrue(dto.hasError());
        }

        @Test
        void emailMissing() {
            AccountCreationManager m = givenAManager(givenAnAccountCreator(false, false));
            TemporaryAccountCreationResultDto dto = m.create(new TemporaryAccountDto("", "passwordok", null, "en"));
            Assertions.assertTrue(dto.isEmailMissing());
            Assertions.assertTrue(dto.hasError());
        }

        @Test
        void emailExisting() {
            AccountCreationManager m = givenAManager(givenAnAccountCreator(false, true));
            TemporaryAccountCreationResultDto dto = m.create(new TemporaryAccountDto("loginok", "passwordok", "me@me.com", "en"));
            Assertions.assertTrue(dto.isEmailExisting());
            Assertions.assertTrue(dto.hasError());
        }

        @Test
        void technicalError() {
            AccountCreationManager m = givenAManager(givenAnAccountCreator(false, false, true));
            TemporaryAccountCreationResultDto dto = m.create(new TemporaryAccountDto("loginok", "passwordok", "me@me.com", "en"));
            Assertions.assertTrue(dto.isTechnicalIssue());
            Assertions.assertTrue(dto.hasError());
        }
    }

    private static AccountCreator givenAnAccountCreator(boolean loginExist, boolean emailExist) {
        return givenAnAccountCreator(loginExist, emailExist, false);
    }

    private static AccountCreator givenAnAccountCreator(boolean loginExist, boolean emailExist, boolean error) {
        return new AccountCreator() {
            @Override
            public boolean loginAlreadyExist(String login) {
                return loginExist;
            }

            @Override
            public boolean emailAlreadyExist(String email) {
                return emailExist;
            }

            @Override
            public void create(TemporaryAccountDto dto, UUID token) {
                if(error) {
                    throw new PersistenceException(new Exception("Boum"));
                }
            }

            @Override
            public void validate(AccountConfirmationDto validation) {

            }
        };
    }

    private static AccountCreationManager givenAManager(AccountCreator c) {
        return new AccountCreationManager(c, (m) -> {}, (l) -> givenATemplatePath());
    }

    private static Path givenATemplatePath() {
        try {
            return Paths.get(ClassLoader.getSystemResource("mail.txt").toURI());
        } catch (URISyntaxException e) {
            Assertions.fail(e);
            return null;
        }
    }
}
