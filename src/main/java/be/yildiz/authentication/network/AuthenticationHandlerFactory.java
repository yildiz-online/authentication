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

package be.yildiz.authentication.network;

import be.yildiz.authentication.AuthenticationManager;
import be.yildiz.module.network.netty.HandlerFactory;
import be.yildiz.module.network.netty.server.SessionMessageHandler;
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

    public AuthenticationHandlerFactory(AuthenticationManager manager) {
        this.manager = manager;
    }

    /**
     * Create a new AuthenticationHandler to be associated with client sessions.
     *
     * @return The created handler.
     */
    @Override
    public ChannelHandler create() {
        return new SessionMessageHandler(new AuthenticationHandler(this.manager));
    }

}
