/*
 *
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 * Copyright (c) 2019 Grégory Van den Borre
 *
 * More infos available: https://engine.yildiz-games.be
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 *  WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT  HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE  SOFTWARE.
 *
 *
 */

package be.yildizgames.authentication.infrastructure;

import be.yildizgames.authentication.application.AccountCreationManager;
import be.yildizgames.authentication.application.AuthenticationManager;
import be.yildizgames.common.authentication.Credentials;
import be.yildizgames.common.authentication.Token;
import be.yildizgames.common.authentication.protocol.Queues;
import be.yildizgames.common.authentication.protocol.TemporaryAccountCreationResultDto;
import be.yildizgames.common.authentication.protocol.mapper.CredentialsMapper;
import be.yildizgames.common.authentication.protocol.mapper.TemporaryAccountResultMapper;
import be.yildizgames.common.authentication.protocol.mapper.TokenMapper;
import be.yildizgames.common.authentication.protocol.mapper.exception.AuthenticationMappingException;
import be.yildizgames.common.exception.technical.TechnicalException;
import be.yildizgames.module.messaging.Broker;
import be.yildizgames.module.messaging.BrokerMessageDestination;
import be.yildizgames.module.messaging.BrokerMessageHeader;
import be.yildizgames.module.messaging.BrokerMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Grégory Van den Borre
 */
public class AsynchronousAuthenticationServer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public AsynchronousAuthenticationServer(Broker broker, AccountCreationManager accountCreationManager, AuthenticationManager authenticationManager) {
        BrokerMessageDestination temporaryAccountCreatedQueue = broker.registerQueue(Queues.ACCOUNT_CREATION_TEMP.getName());
        BrokerMessageDestination accountCreationRequestQueue = broker.registerQueue(Queues.CREATE_ACCOUNT_REQUEST.getName());
        BrokerMessageDestination authenticationRequestQueue = broker.registerQueue(Queues.AUTHENTICATION_REQUEST.getName());
        BrokerMessageDestination authenticationResponseQueue = broker.registerQueue(Queues.AUTHENTICATION_RESPONSE.getName());

        BrokerMessageProducer tempProducer = temporaryAccountCreatedQueue.createProducer();

        accountCreationRequestQueue.createConsumer(message -> {
            try {
                logger.debug("message received in {}: {}",Queues.CREATE_ACCOUNT_REQUEST.getName(), message.getText());

                TemporaryAccountCreationResultDto result = accountCreationManager.create(this.from(message.getText()));
                tempProducer.sendMessage(TemporaryAccountResultMapper.getInstance().to(result), BrokerMessageHeader.correlationId(message.getCorrelationId()));
            } catch (TechnicalException e) {
                logger.warn("Unexpected message", e);
            }
        });
        BrokerMessageProducer authenticationResponseProducer = authenticationResponseQueue.createProducer();
        authenticationRequestQueue.createConsumer(message -> {
            logger.debug("message received in {}: {}",Queues.AUTHENTICATION_REQUEST.getName(), message.getText());
            try {
                Credentials r = CredentialsMapper.getInstance().from(message.getText());
                Token token = authenticationManager.authenticate(r);
                logger.debug("Send authentication response message to {} : {}", token.getId(), token.getStatus());
                authenticationResponseProducer.sendMessage(TokenMapper.getInstance().to(token), BrokerMessageHeader.correlationId(message.getCorrelationId()));
            } catch (TechnicalException e) {
                logger.warn("Unexpected message", e);
            }
        });
    }

    public TemporaryAccountDto from(String s) {
        assert s != null;
        try {
            String[] v = s.split("@@");
            return new TemporaryAccountDto(v[0], v[1], v[2], v[3]);
        } catch (IndexOutOfBoundsException var3) {
            throw new AuthenticationMappingException(var3);
        }
    }

}
