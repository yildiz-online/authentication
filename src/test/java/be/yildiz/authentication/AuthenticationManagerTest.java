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

package be.yildiz.authentication;

import be.yildiz.common.Token;
import be.yildiz.common.authentication.Credentials;
import be.yildiz.common.exeption.NotFoundException;
import be.yildiz.common.id.PlayerId;
import be.yildiz.module.network.protocol.AuthenticationRequest;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Grégory Van den Borre
 */
public class AuthenticationManagerTest {


    @Test
    public void testAuthenticate() {
        Token result = this.givenAuthenticatedResult("test", "test1");
        this.thenResultIsAuthenticated(result);
    }

    @Test
    public void testAuthenticateNot() {
        Token result = this.givenNotAuthenticatedResult("test", "test1");
        this.thenResultIsNotAuthenticated(result);
    }

    @Test
    public void testAuthenticateNotFound() {
        Token result = this.givenNotFoundResult("test", "test1");
        this.thenResultIsNotFound(result);
    }

    @Test
    public void testAuthenticateInvalidInput() {
        Token result = this.givenAuthenticatedResult("", "");
        this.thenResultIsNotFound(result);
    }

    @Test
    public void testAuthenticateBan() {
        AuthenticatorMock mock = new AuthenticatorMock(false, false);
        AuthenticationManager m = new AuthenticationManager(mock);
        for (int i = 0; i < 5; i++) {
            Token result = m.authenticate(new AuthenticationRequest("test", "test1"));
            this.thenResultIsNotAuthenticated(result);
        }
        Token result = m.authenticate(new AuthenticationRequest("test", "test1"));
        this.thenResultIsBanned(result);
        mock.toReturn = true;
        result = m.authenticate(new AuthenticationRequest("test", "test1"));
        this.thenResultIsBanned(result);
    }

    @Test
    public void testAuthenticateResetInvalidInput() {
        AuthenticatorMock mock = new AuthenticatorMock(false, false);
        AuthenticationManager m = new AuthenticationManager(mock);
        for (int i = 0; i < 4; i++) {
            Token result = m.authenticate(new AuthenticationRequest("test", "test1"));
            this.thenResultIsNotAuthenticated(result);
        }
        mock.toReturn = true;
        Token result = m.authenticate(new AuthenticationRequest("test", "test1"));
        this.thenResultIsAuthenticated(result);
        mock.toReturn = false;
        for (int i = 0; i < 5; i++) {
            result = m.authenticate(new AuthenticationRequest("test", "test1"));
            this.thenResultIsNotAuthenticated(result);
        }
    }

    @Test
    public void testGetAuthenticated() {
        //fail("Not yet implemented");
    }

    private Token givenAuthenticatedResult(final String login, final String pwd) {
        AuthenticationManager m = new AuthenticationManager(new AuthenticatorMock(true, false));
        return m.authenticate(new AuthenticationRequest(login, pwd));
    }

    private Token givenNotAuthenticatedResult(final String login, final String pwd) {
        AuthenticationManager m = new AuthenticationManager(new AuthenticatorMock(false, false));
        return m.authenticate(new AuthenticationRequest("test", "test"));
    }

    private Token givenNotFoundResult(final String login, final String pwd) {
        AuthenticationManager m = new AuthenticationManager(new AuthenticatorMock(true, true));
        return m.authenticate(new AuthenticationRequest("test", "test"));
    }

    private void thenResultIsNotFound(final Token result) {
        Assert.assertFalse(result.isAuthenticated());
        Assert.assertEquals(Token.Status.NOT_FOUND, result.getStatus());
    }

    private void thenResultIsBanned(final Token result) {
        Assert.assertFalse(result.isAuthenticated());
        Assert.assertEquals(Token.Status.BANNED, result.getStatus());
    }

    private void thenResultIsNotAuthenticated(Token result) {
        Assert.assertFalse(result.isAuthenticated());
        Assert.assertEquals(Token.Status.NOT_AUTHENTICATED, result.getStatus());
    }

    private void thenResultIsAuthenticated(Token result) {
        Assert.assertTrue(result.isAuthenticated());
        Assert.assertEquals(Token.Status.AUTHENTICATED, result.getStatus());
    }

    private static class AuthenticatorMock implements Authenticator {

        private boolean toReturn;

        private boolean notFound;

        public AuthenticatorMock(final boolean toReturn, final boolean notFound) {
            super();
            this.toReturn = toReturn;
            this.notFound = notFound;
        }

        @Override
        public AuthenticationResult getPasswordForUser(Credentials credentials) throws NotFoundException {
            if (notFound) {
                throw new NotFoundException();
            }
            return new AuthenticationResult(toReturn, PlayerId.WORLD);
        }

    }

}
