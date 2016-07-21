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
import be.yildiz.module.network.netty.DecoderEncoder;
import be.yildiz.module.network.netty.factory.NettyFactory;
import be.yildiz.module.network.netty.server.ServerNetty;

/**
 * Create a server using authentication handlers.
 *
 * @author Grégory Van den Borre
 */
public final class AuthenticationServer {

    /**
     * Associated base server.
     */
    private final ServerNetty server;

    /**
     * Create a new server, with a given port.
     *
     * @param port    Port to use to listen to authentication requests, and authentication verification requests.
     * @param manager Manager to use for the authentication process.
     * @requires 0 >= port <= 65635
     * @requires manager != null
     * @ensures to create a new server.
     */
    public AuthenticationServer(final String address, final int port, final AuthenticationManager manager) {
        super();
        this.server = NettyFactory.createServerNetty(address, port, new AuthenticationHandlerFactory(manager, DecoderEncoder.WEBSOCKET));
    }

    /**
     * Start listening for incoming requests.
     */
    public void startServer() {
        this.server.startServer();
    }

    /**
     * @return The server port number.
     */
    public int getPort() {
        return server.getPort();
    }

    /**
     * @return The server host address.
     */
    public String getHost() {
        return server.getAddress();
    }
}
