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

package be.yildiz.authentication.it;

import be.yildiz.module.messaging.Broker;
import be.yildiz.module.messaging.BrokerMessageDestination;
import be.yildiz.module.network.client.NetworkListener;
import be.yildiz.module.network.exceptions.InvalidNetworkMessage;
import be.yildiz.module.network.netty.client.ClientNetty;
import be.yildiz.module.network.netty.factory.NettyFactory;
import be.yildiz.module.network.protocol.AccountValidationDto;
import be.yildiz.module.network.protocol.MessageWrapper;
import be.yildiz.module.network.protocol.NetworkMessage;
import be.yildiz.module.network.protocol.NetworkMessageFactory;

/**
 * @author Grégory Van den Borre
 */
public class ManualTest {

    public static void main(String[] args) throws Exception {
        NetworkMessageFactory factory = new NetworkMessageFactory();
        NetworkMessage message = factory.accountValidation(new AccountValidationDto("mylogin", "8b7e1f34-bf04-4bba-b528-bf18f046b563"));
        ClientNetty client = NettyFactory.createClientNetty();
        Broker broker = Broker.initialize("localhost", 61616);
        BrokerMessageDestination destination = broker.registerQueue("authentication-creation");
        destination.createConsumer((m) -> System.out.println(m));
        client.addNetworkListener(new NetworkListener() {
            @Override
            public void parse(MessageWrapper message) throws InvalidNetworkMessage {

            }

            @Override
            public void connected() {
                client.sendMessage(message);
            }
        });
        client.connect("localhost", 15023);
        while(true) {
            client.update();
            Thread.sleep(100);
        }
    }

}
