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

package be.yildiz.authentication;

import be.yildiz.common.authentication.*;
import be.yildiz.common.exeption.NotFoundException;
import be.yildiz.common.exeption.TechnicalException;
import be.yildiz.common.id.PlayerId;
import be.yildiz.common.log.Logger;
import be.yildiz.module.database.DataBaseConnectionProvider;
import be.yildiz.module.database.TestingDatabaseInit;
import be.yildiz.module.network.protocol.TokenVerification;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.sql.SQLException;

/**
 * @author Grégory Van den Borre
 */
@RunWith(Enclosed.class)
public class DatabaseAuthenticatorTest {

    public static class Constructor {

        @Before
        public void init() {
            Logger.disable();
        }

        private DataBaseConnectionProvider givenAConnexionProvider() throws SQLException {
            return new TestingDatabaseInit().init("test_db.xml");
        }

        @Test
        public void happyFlow() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                new DataBaseAuthenticator(dbcp, "blabla");
            }
        }

        @Test
        public void withNoKey() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                new DataBaseAuthenticator(dbcp);
            }
        }

        @Test(expected = AssertionError.class)
        public void withNullProvider() {
            new DataBaseAuthenticator(null);
        }

        @Test
        public void withNullKey() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                new DataBaseAuthenticator(dbcp, null);
            }
        }
    }

    public static class GetPasswordForUser {

        @Before
        public void init() {
            Logger.disable();
        }

        private DataBaseConnectionProvider givenAConnexionProvider() throws SQLException {
            return new TestingDatabaseInit().init("test_db.xml");
        }

        private Credentials givenCredentials(String login, String password) throws CredentialException {
            String encPassword = new BCryptEncryptionTool().encrypt(password);
            System.out.println(encPassword);
            return Credentials.unchecked(login, encPassword);
        }

        @Test(expected = AssertionError.class)
        public void withNullCredentials() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DataBaseAuthenticator da = new DataBaseAuthenticator(dbcp);
                da.getPasswordForUser(null);
            }
        }

        @Test(expected = NotFoundException.class)
        public void withNotFoundCredentials() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DataBaseAuthenticator da = new DataBaseAuthenticator(dbcp);
                da.getPasswordForUser(givenCredentials("azerty", "azerty"));
            }
        }

        @Ignore
        @Test
        public void withWrongCredentials() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DataBaseAuthenticator da = new DataBaseAuthenticator(dbcp);
                TokenVerification v = da.getPasswordForUser(givenCredentials("existing", "azerty"));
                Assert.assertFalse(v.authenticated);
                Assert.assertEquals(PlayerId.valueOf(1), v.playerId);
            }
        }

        @Ignore
        @Test
        public void withRightCredentials() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DataBaseAuthenticator da = new DataBaseAuthenticator(dbcp);
                TokenVerification v = da.getPasswordForUser(givenCredentials("existing", "rightPassword"));
                Assert.assertTrue(v.authenticated);
                Assert.assertEquals(PlayerId.valueOf(1), v.playerId);
            }
        }

        @Test(expected = NotFoundException.class)
        public void withRightCredentialsButInactive() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DataBaseAuthenticator da = new DataBaseAuthenticator(dbcp);
                da.getPasswordForUser(givenCredentials("existingInactive", "rightPassword"));
            }
        }

        @Test
        public void withGenericKey() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DataBaseAuthenticator da = new DataBaseAuthenticator(dbcp, "magic");
                TokenVerification v = da.getPasswordForUser(Credentials.unchecked("existing", "magic"));
                Assert.assertTrue(v.authenticated);
                Assert.assertEquals(PlayerId.valueOf(1), v.playerId);
            }
        }

        @Test(expected = TechnicalException.class)
        public void withInvalidSalt() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DataBaseAuthenticator da = new DataBaseAuthenticator(dbcp);
                da.getPasswordForUser(Credentials.unchecked("existing", "azerty"));
            }
        }
    }
}
