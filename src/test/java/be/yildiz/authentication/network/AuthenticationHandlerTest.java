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

package be.yildiz.authentication.network;

import be.yildiz.authentication.AuthenticationManager;
import be.yildiz.authentication.Authenticator;
import be.yildiz.common.Token;
import be.yildiz.common.id.PlayerId;
import be.yildiz.module.network.protocol.*;
import be.yildiz.module.network.server.Session;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Grégory Van den Borre
 */
public final class AuthenticationHandlerTest {

    @Test
    public void messageReceivedImplAuthenticationRequest() throws Exception {
        NetworkMessageFactory f = new NetworkMessageFactory();
        Authenticator auth = Mockito.mock(Authenticator.class);
        Mockito
                .when(auth.getPasswordForUser(Mockito.any()))
                .thenReturn(new TokenVerification(PlayerId.valueOf(5), true));
        AuthenticationManager manager = new AuthenticationManager(auth);
        Token token = Token.authenticated(PlayerId.valueOf(5), 200L, 123);
        NetworkMessage<Authentication> request = f.authenticationRequest(new Authentication("abc", "abcde"));
        Mockito
                .when(manager.authenticate(request.getDto()))
                .thenReturn(token);
        AuthenticationHandler handler = new AuthenticationHandler(manager);
        Session session = Mockito.mock(Session.class);
        handler.messageReceivedImpl(session, new MessageWrapper(request.buildMessage()));

        Mockito.verify(session).sendMessage(f.authenticationResponse(token));
    }

    @Test
    public void messageReceivedImplTokenVerificationRequest() {
        NetworkMessageFactory f = new NetworkMessageFactory();
        Authenticator auth = Mockito.mock(Authenticator.class);
        AuthenticationManager manager = new AuthenticationManager(auth);
        Token token = Token.authenticated(PlayerId.valueOf(5), 200L, 123);
        NetworkMessage<Token> request = f.tokenVerification(token);
        AuthenticationHandler handler = new AuthenticationHandler(manager);
        Session session = Mockito.mock(Session.class);
        handler.messageReceivedImpl(session, new MessageWrapper(request.buildMessage()));
        Mockito.verify(session).sendMessage(f.tokenVerified(new TokenVerification(PlayerId.valueOf(5), true)));
    }

    @Test
    public void messageReceivedImplOtherRequest() {
//        Assert.fail();
    }

    @Test
    public void messageReceivedImplInvalidMessage() {
//        Assert.fail();
    }

}
