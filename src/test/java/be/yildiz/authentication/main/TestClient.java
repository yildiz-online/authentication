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

package be.yildiz.authentication.main;

import be.yildiz.module.network.client.NetworkListener;
import be.yildiz.module.network.exceptions.InvalidNetworkMessage;
import be.yildiz.module.network.netty.client.ClientNetty;
import be.yildiz.module.network.netty.factory.NettyFactory;
import be.yildiz.module.network.protocol.AuthenticationRequest;
import be.yildiz.module.network.protocol.AuthenticationResponse;
import be.yildiz.module.network.protocol.MessageWrapper;

/**
 * @author Grégory Van den Borre
 */
public class TestClient implements NetworkListener {

    //FIXME change to integration test, use cucumber

    private ClientNetty client;

    public TestClient(ClientNetty client) {
        super();
        this.client = client;
    }

    public static void main(String[] args) {
        ClientNetty client = NettyFactory.createClientNetty();
        client.addNetworkListener(new TestClient(client));
        client.connect("192.168.1.4", 15023);
        client.sendMessage(new AuthenticationRequest("pouet", "abcd"));

        while (client.isConnected()) {
            client.update();
        }
    }

    @Override
    public void parse(MessageWrapper message) throws InvalidNetworkMessage {
        AuthenticationResponse r = new AuthenticationResponse(message);
        System.out.println(r.getToken().getId() + ":" + r.getToken().getKey() + ":" + r.getToken().getStatus());
        client.close();
    }

    @Override
    public void connectionLost() {
        System.out.println("connection closed");

    }

    @Override
    public void connectionFailed() {
    }

    @Override
    public void connected() {
        System.out.println("connected");
    }
}
