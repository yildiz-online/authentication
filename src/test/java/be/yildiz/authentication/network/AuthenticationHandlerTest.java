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

import be.yildiz.authentication.AccountCreationManager;
import be.yildiz.authentication.AuthenticationManager;
import be.yildiz.authentication.DummyAccountCreator;
import be.yildiz.authentication.DummyEmailService;
import be.yildiz.common.Token;
import be.yildiz.common.authentication.AuthenticationRules;
import be.yildiz.common.authentication.Credentials;
import be.yildiz.common.id.PlayerId;
import be.yildiz.module.network.protocol.NetworkMessage;
import be.yildiz.module.network.protocol.NetworkMessageFactory;
import be.yildiz.module.network.protocol.TokenVerification;
import be.yildiz.module.network.server.Session;
import be.yildizgames.common.mapping.IntegerMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Grégory Van den Borre
 */
final class AuthenticationHandlerTest {

    @Test
    void messageReceivedImplAuthenticationRequest() {
        NetworkMessageFactory f = new NetworkMessageFactory();
        AuthenticationManager manager = new AuthenticationManager(c -> new TokenVerification(PlayerId.valueOf(5), true));
        AccountCreationManager accountCreationManager = new AccountCreationManager(new DummyAccountCreator(), AuthenticationRules.DEFAULT, new DummyEmailService());
        AuthenticationHandler handler = new AuthenticationHandler(manager, accountCreationManager);
        Session session = Mockito.mock(Session.class);
        NetworkMessage<Credentials> request = f.authenticationRequest(Credentials.unchecked("abc", "abcde"));
        handler.processMessages(session, request.buildMessage());
        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        Mockito.verify(session, Mockito.times(1)).sendMessage(ac.capture());
        assertTrue(ac.getValue().startsWith("&99_5@") && ac.getValue().endsWith("@0#"));
    }

    @Test
    void messageReceivedImplTokenVerificationRequestNegative() {
        NetworkMessageFactory f = new NetworkMessageFactory();
        AuthenticationManager manager = new AuthenticationManager(c -> new TokenVerification(PlayerId.valueOf(5), true));
        Token token = Token.authenticated(PlayerId.valueOf(5), 200L, 123);
        AccountCreationManager accountCreationManager = new AccountCreationManager(new DummyAccountCreator(), AuthenticationRules.DEFAULT, new DummyEmailService());
        AuthenticationHandler handler = new AuthenticationHandler(manager, accountCreationManager);
        Session session = Mockito.mock(Session.class);
        NetworkMessage<Token> request = f.tokenVerification(token);
        handler.processMessages(session, request.buildMessage());
        // not authenticated before, so token verification is false
        Mockito.verify(session, Mockito.times(1)).sendMessage(f.tokenVerified(new TokenVerification(PlayerId.valueOf(5), false)));
    }

    @Test
    void messageReceivedImplTokenVerificationRequestPositive() {
        NetworkMessageFactory f = new NetworkMessageFactory();
        AuthenticationManager manager = new AuthenticationManager(c -> new TokenVerification(PlayerId.valueOf(5), true));
        Token token = manager.authenticate(Credentials.unchecked("abc", "abcde"));
        AccountCreationManager accountCreationManager = new AccountCreationManager(new DummyAccountCreator(), AuthenticationRules.DEFAULT, new DummyEmailService());
        AuthenticationHandler handler = new AuthenticationHandler(manager, accountCreationManager);
        Session session = Mockito.mock(Session.class);
        NetworkMessage<Token> request = f.tokenVerification(token);
        handler.processMessages(session, request.buildMessage());
        // not authenticated before, so token verification is false
        Mockito.verify(session, Mockito.times(1)).sendMessage(f.tokenVerified(new TokenVerification(PlayerId.valueOf(5), true)));
    }

    @Test
    void messageReceivedImplOtherRequest() {
        AuthenticationManager manager = new AuthenticationManager(c -> new TokenVerification(PlayerId.valueOf(5), true));
        AccountCreationManager accountCreationManager = new AccountCreationManager(new DummyAccountCreator(), AuthenticationRules.DEFAULT, new DummyEmailService());
        AuthenticationHandler handler = new AuthenticationHandler(manager, accountCreationManager);
        SessionMock session = new SessionMock(PlayerId.valueOf(5));
        NetworkMessage<Integer> request = new NetworkMessage<>(7, IntegerMapper.getInstance(), 45);
        handler.processMessages(session, request.buildMessage());
        assertEquals(0, session.invocation);
        assertFalse(session.isConnected());
    }

    @Test
    void messageReceivedImplInvalidMessage() {
        AuthenticationManager manager = new AuthenticationManager(c -> new TokenVerification(PlayerId.valueOf(5), true));
        AccountCreationManager accountCreationManager = new AccountCreationManager(new DummyAccountCreator(), AuthenticationRules.DEFAULT, new DummyEmailService());
        AuthenticationHandler handler = new AuthenticationHandler(manager, accountCreationManager);
        SessionMock session = new SessionMock(PlayerId.valueOf(5));
        handler.processMessages(session, "&10_abc#");
        assertEquals(0, session.invocation);
        assertFalse(session.isConnected());
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
