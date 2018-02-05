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

import be.yildizgames.common.authentication.Credentials;
import be.yildizgames.common.authentication.Token;
import be.yildizgames.common.authentication.UserNotFoundException;
import be.yildizgames.common.authentication.protocol.TokenVerification;
import be.yildizgames.common.model.PlayerId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Grégory Van den Borre
 */
class AuthenticationManagerTest {

    @Test
    void withNullAuthenticator() {
        assertThrows(AssertionError.class, () -> new AuthenticationManager(null));
    }

    @Test
    void testAuthenticate() {
        Token result = this.givenAuthenticatedResult("test", "test1");
        this.thenResultIsAuthenticated(result);
    }

    @Test
    void testAuthenticateNot() {
        Token result = this.givenNotAuthenticatedResult("test", "test1");
        this.thenResultIsNotAuthenticated(result);
    }

    @Test
    void testAuthenticateNotFound() {
        Token result = this.givenNotFoundResult("test", "test1");
        this.thenResultIsNotFound(result);
    }

    @Test
    void testAuthenticateInvalidInput() {
        Token result = this.givenAuthenticatedResult("", "");
        this.thenResultIsNotFound(result);
    }

    @Test
    void testAuthenticateBan() {
        AuthenticatorMock mock = new AuthenticatorMock(false, false);
        AuthenticationManager m = new AuthenticationManager(mock);
        for (int i = 0; i < 5; i++) {
            Token result = m.authenticate(Credentials.unchecked("test", "test1"));
            this.thenResultIsNotAuthenticated(result);
        }
        Token result = m.authenticate(Credentials.unchecked("test", "test1"));
        this.thenResultIsBanned(result);
        mock.toReturn = true;
        result = m.authenticate(Credentials.unchecked("test", "test1"));
        this.thenResultIsBanned(result);
    }

    @Test
    void testAuthenticateResetInvalidInput() {
        AuthenticatorMock mock = new AuthenticatorMock(false, false);
        AuthenticationManager m = new AuthenticationManager(mock);
        for (int i = 0; i < 4; i++) {
            Token result = m.authenticate(Credentials.unchecked("test", "test1"));
            this.thenResultIsNotAuthenticated(result);
        }
        mock.toReturn = true;
        Token result = m.authenticate(Credentials.unchecked("test", "test1"));
        this.thenResultIsAuthenticated(result);
        mock.toReturn = false;
        for (int i = 0; i < 5; i++) {
            result = m.authenticate(Credentials.unchecked("test", "test1"));
            this.thenResultIsNotAuthenticated(result);
        }
    }

    @Test
    void testGetAuthenticated() {
        //fail("Not yet implemented");
    }

    private Token givenAuthenticatedResult(final String login, final String pwd) {
        AuthenticationManager m = new AuthenticationManager(new AuthenticatorMock(true, false));
        return m.authenticate(Credentials.unchecked(login, pwd));
    }

    private Token givenNotAuthenticatedResult(final String login, final String pwd) {
        AuthenticationManager m = new AuthenticationManager(new AuthenticatorMock(false, false));
        return m.authenticate(Credentials.unchecked("test", "test1"));
    }

    private Token givenNotFoundResult(final String login, final String pwd) {
        AuthenticationManager m = new AuthenticationManager(new AuthenticatorMock(true, true));
        return m.authenticate(Credentials.unchecked("test", "test1"));
    }

    private void thenResultIsNotFound(final Token result) {
        assertFalse(result.isAuthenticated());
        assertEquals(Token.Status.NOT_FOUND, result.getStatus());
    }

    private void thenResultIsBanned(final Token result) {
        assertFalse(result.isAuthenticated());
        assertEquals(Token.Status.BANNED, result.getStatus());
    }

    private void thenResultIsNotAuthenticated(Token result) {
        assertFalse(result.isAuthenticated());
        assertEquals(Token.Status.NOT_AUTHENTICATED, result.getStatus());
    }

    private void thenResultIsAuthenticated(Token result) {
        assertTrue(result.isAuthenticated());
        assertEquals(Token.Status.AUTHENTICATED, result.getStatus());
    }

    private static class AuthenticatorMock implements Authenticator {

        private boolean toReturn;

        private boolean notFound;

        AuthenticatorMock(final boolean toReturn, final boolean notFound) {
            super();
            this.toReturn = toReturn;
            this.notFound = notFound;
        }

        @Override
        public TokenVerification getPasswordForUser(Credentials credentials) throws UserNotFoundException {
            if (notFound) {
                throw new UserNotFoundException();
            }
            return new TokenVerification(PlayerId.WORLD, toReturn);
        }

    }

}
