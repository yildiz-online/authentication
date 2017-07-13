/*
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 *  Copyright (c) 2017 Grégory Van den Borre
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

package be.yildiz.authentication.network;

import be.yildiz.authentication.AuthenticationManager;
import be.yildiz.common.id.PlayerId;
import be.yildiz.module.network.netty.DecoderEncoder;
import be.yildiz.module.network.netty.server.SessionMessageHandler;
import be.yildiz.module.network.netty.server.SessionWebSocketMessageHandler;
import be.yildiz.module.network.protocol.TokenVerification;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * @author Grégory Van den Borre
 */
@RunWith(Enclosed.class)
public class AuthenticationHandlerFactoryTest {

    public static class Constructor {

        @Test
        public void happyFlow(){
            AuthenticationHandlerFactory factory = new AuthenticationHandlerFactory(new AuthenticationManager(c -> new TokenVerification(PlayerId.valueOf(5), true)), DecoderEncoder.WEBSOCKET);
            Assert.assertEquals(DecoderEncoder.WEBSOCKET, factory.getCodec());
            Assert.assertTrue(factory.isServer());
        }

        @Test(expected = AssertionError.class)
        public void nullManager() {
            new AuthenticationHandlerFactory(null, DecoderEncoder.WEBSOCKET);
        }

        @Test(expected = AssertionError.class)
        public void nullCodec() {
            new AuthenticationHandlerFactory(new AuthenticationManager(c -> new TokenVerification(PlayerId.valueOf(5), true)), null);
        }
    }

    public static class Create {

        @Test
        public void websocket() {
            AuthenticationHandlerFactory factory = new AuthenticationHandlerFactory(
                    new AuthenticationManager(c -> new TokenVerification(PlayerId.valueOf(5), true)), DecoderEncoder.WEBSOCKET);
            Assert.assertTrue(factory.create() instanceof SessionWebSocketMessageHandler);
        }

        @Test
        public void string() {
            AuthenticationHandlerFactory factory = new AuthenticationHandlerFactory(
                    new AuthenticationManager(c -> new TokenVerification(PlayerId.valueOf(5), true)), DecoderEncoder.STRING);
            Assert.assertTrue(factory.create() instanceof SessionMessageHandler);
        }
    }
}