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
import be.yildiz.module.network.netty.DecoderEncoder;
import be.yildiz.module.network.netty.HandlerFactory;
import be.yildiz.module.network.netty.server.SessionMessageHandler;
import be.yildiz.module.network.netty.server.SessionWebSocketMessageHandler;
import io.netty.channel.ChannelHandler;

/**
 * Create instances of AuthenticationHandler to be used by the server.
 *
 * @author Grégory Van den Borre
 */
public final class AuthenticationHandlerFactory implements HandlerFactory {

    /**
     * Associated manager to provide the authentication logic.
     */
    private final AuthenticationManager manager;
    private final DecoderEncoder codec;

    public AuthenticationHandlerFactory(AuthenticationManager manager, DecoderEncoder codec) {
        assert manager != null;
        assert codec != null;
        this.manager = manager;
        this.codec = codec;
    }

    /**
     * Create a new AuthenticationHandler to be associated with client sessions.
     *
     * @return The created handler.
     */
    @Override
    public ChannelHandler create() {
        if(codec == DecoderEncoder.WEBSOCKET) {
            return new SessionWebSocketMessageHandler(new AuthenticationHandler(this.manager));
        }
        return new SessionMessageHandler(new AuthenticationHandler(this.manager));
    }

    @Override
    public DecoderEncoder getCodec() {
        return this.codec;
    }

    @Override
    public boolean isServer() {
        return true;
    }


}
