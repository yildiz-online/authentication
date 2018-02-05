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

package be.yildiz.authentication;

import be.yildiz.module.database.DataBaseConnectionProvider;
import be.yildiz.module.database.TestingDatabaseInit;
import be.yildizgames.common.authentication.CredentialException;
import be.yildizgames.common.authentication.Credentials;
import be.yildizgames.common.authentication.UserNotFoundException;
import be.yildizgames.common.authentication.protocol.TokenVerification;
import be.yildizgames.common.exception.technical.TechnicalException;
import be.yildizgames.common.model.PlayerId;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Grégory Van den Borre
 */
@Disabled
@Tag("database")
class DatabaseAuthenticatorTest {

    @Nested
    class Constructor {

        private DataBaseConnectionProvider givenAConnexionProvider() throws Exception {
            Thread.sleep(500);
            return new TestingDatabaseInit().init("test_db.xml");
        }

        @Test
        void happyFlow() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                new DataBaseAuthenticator(dbcp, "blabla");
            }
        }

        @Test
        void withNoKey() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                new DataBaseAuthenticator(dbcp);
            }
        }

        @Test
        void withNullProvider() {
            assertThrows(AssertionError.class, () -> new DataBaseAuthenticator(null));
        }

        @Test
        void withNullKey() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                new DataBaseAuthenticator(dbcp, null);
            }
        }
    }

    @Nested
    class GetPasswordForUser {

        private DataBaseConnectionProvider givenAConnexionProvider() throws SQLException {
            return new TestingDatabaseInit().init("test_db.xml");
        }

        private Credentials givenCredentials(String login, String password) throws CredentialException {
            return Credentials.unchecked(login, password);
        }

        @Test
        void withNullCredentials() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DataBaseAuthenticator da = new DataBaseAuthenticator(dbcp);
                assertThrows(AssertionError.class, () -> da.getPasswordForUser(null));
            }
        }

        @Test
        void withNotFoundCredentials() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DataBaseAuthenticator da = new DataBaseAuthenticator(dbcp);
                assertThrows(UserNotFoundException.class, () -> da.getPasswordForUser(givenCredentials("azerty", "azerty")));
            }
        }

        @Test
        void withWrongCredentials() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DataBaseAuthenticator da = new DataBaseAuthenticator(dbcp);
                TokenVerification v = da.getPasswordForUser(givenCredentials("existing", "azerty"));
                assertFalse(v.authenticated);
                assertEquals(PlayerId.valueOf(1), v.userId);
            }
        }

        @Test
        void withRightCredentials() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DataBaseAuthenticator da = new DataBaseAuthenticator(dbcp);
                TokenVerification v = da.getPasswordForUser(givenCredentials("existing", "rightPassword"));
                assertTrue(v.authenticated);
                assertEquals(PlayerId.valueOf(1), v.userId);
            }
        }

        @Test
        void withRightCredentialsButInactive() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DataBaseAuthenticator da = new DataBaseAuthenticator(dbcp);
                assertThrows(UserNotFoundException.class, () -> da.getPasswordForUser(givenCredentials("existingInactive", "rightPassword")));
            }
        }

        @Test
        void withGenericKey() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DataBaseAuthenticator da = new DataBaseAuthenticator(dbcp, "magic");
                TokenVerification v = da.getPasswordForUser(Credentials.unchecked("existing", "magic"));
                assertTrue(v.authenticated);
                assertEquals(PlayerId.valueOf(1), v.userId);
            }
        }

        @Test
        void withInvalidSalt() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DataBaseAuthenticator da = new DataBaseAuthenticator(dbcp);
                assertThrows(TechnicalException.class, () -> da.getPasswordForUser(Credentials.unchecked("invalidSalt", "azerty")));
            }
        }
    }
}
