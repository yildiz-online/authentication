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
import be.yildiz.common.Token;
import be.yildiz.common.authentication.Credentials;
import be.yildiz.common.id.PlayerId;
import be.yildiz.common.log.Logger;
import be.yildiz.module.network.protocol.NetworkMessage;
import be.yildiz.module.network.protocol.NetworkMessageFactory;
import be.yildiz.module.network.protocol.TokenVerification;
import be.yildiz.module.network.protocol.mapper.IntegerMapper;
import be.yildiz.module.network.server.Session;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * @author Grégory Van den Borre
 */
public final class AuthenticationHandlerTest {

    @Test
    public void messageReceivedImplAuthenticationRequest() throws Exception {
        Logger.disable();
        NetworkMessageFactory f = new NetworkMessageFactory();
        AuthenticationManager manager = new AuthenticationManager(c -> new TokenVerification(PlayerId.valueOf(5), true));
        AuthenticationHandler handler = new AuthenticationHandler(manager);
        Session session = Mockito.mock(Session.class);
        NetworkMessage<Credentials> request = f.authenticationRequest(Credentials.unchecked("abc", "abcde"));
        handler.processMessages(session, request.buildMessage());
        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        Mockito.verify(session, Mockito.times(1)).sendMessage(ac.capture());
        Assert.assertTrue(ac.getValue().startsWith("&99_5@") && ac.getValue().endsWith("@0#"));
    }

    @Test
    public void messageReceivedImplTokenVerificationRequest() {
        Logger.disable();
        NetworkMessageFactory f = new NetworkMessageFactory();
        AuthenticationManager manager = new AuthenticationManager(c -> new TokenVerification(PlayerId.valueOf(5), true));
        Token token = Token.authenticated(PlayerId.valueOf(5), 200L, 123);
        AuthenticationHandler handler = new AuthenticationHandler(manager);
        Session session = Mockito.mock(Session.class);
        NetworkMessage<Token> request = f.tokenVerification(token);
        handler.processMessages(session, request.buildMessage());
        // not authenticated before, so token verification is false
        Mockito.verify(session, Mockito.times(1)).sendMessage(f.tokenVerified(new TokenVerification(PlayerId.valueOf(5), false)));
    }

    @Test
    public void messageReceivedImplOtherRequest() {
        Logger.disable();
        AuthenticationManager manager = new AuthenticationManager(c -> new TokenVerification(PlayerId.valueOf(5), true));
        AuthenticationHandler handler = new AuthenticationHandler(manager);
        SessionMock session = new SessionMock(PlayerId.valueOf(5));
        NetworkMessage<Integer> request = new NetworkMessage<>(7, IntegerMapper.getInstance(), 45);
        handler.processMessages(session, request.buildMessage());
        Assert.assertEquals(0, session.invocation);
        Assert.assertFalse(session.isConnected());
    }

    @Test
    public void messageReceivedImplInvalidMessage() {
        Logger.disable();
        AuthenticationManager manager = new AuthenticationManager(c -> new TokenVerification(PlayerId.valueOf(5), true));
        AuthenticationHandler handler = new AuthenticationHandler(manager);
        SessionMock session = new SessionMock(PlayerId.valueOf(5));
        handler.processMessages(session, "&10_abc#");
        Assert.assertEquals(0, session.invocation);
        Assert.assertFalse(session.isConnected());
    }

    private static class SessionMock extends Session  {

        private int invocation;

        /**
         * Full constructor.
         *
         * @param player Player associated to this session.
         */
        private SessionMock(PlayerId player) {
            super(player);
        }

        @Override
        protected void closeSession() {

        }

        @Override
        public void sendMessage(String message) {
            invocation++;
        }
    }

}
