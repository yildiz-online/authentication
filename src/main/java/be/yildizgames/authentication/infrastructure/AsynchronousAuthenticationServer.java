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
import be.yildizgames.common.authentication.protocol.mapper.AccountConfirmationMapper;
import be.yildizgames.common.authentication.protocol.mapper.CredentialsMapper;
import be.yildizgames.common.authentication.protocol.mapper.TemporaryAccountResultMapper;
import be.yildizgames.common.authentication.protocol.mapper.TokenMapper;
import be.yildizgames.common.exception.technical.TechnicalException;
import be.yildizgames.module.messaging.Broker;
import be.yildizgames.module.messaging.BrokerMessage;
import be.yildizgames.module.messaging.BrokerMessageDestination;
import be.yildizgames.module.messaging.BrokerMessageHeader;
import be.yildizgames.module.messaging.BrokerMessageProducer;

/**
 * @author Grégory Van den Borre
 */
public class AsynchronousAuthenticationServer {

    /**
     * Logger.
     */
    private final System.Logger logger = System.getLogger(AsynchronousAuthenticationServer.class.toString());

    public AsynchronousAuthenticationServer(Broker broker, AccountCreationManager accountCreationManager, AuthenticationManager authenticationManager) {
        BrokerMessageDestination temporaryAccountCreatedQueue = broker.registerQueue(Queues.ACCOUNT_CREATION_TEMP.getName());
        BrokerMessageDestination accountCreationRequestQueue = broker.registerQueue(Queues.CREATE_ACCOUNT_REQUEST.getName());
        BrokerMessageDestination authenticationRequestQueue = broker.registerQueue(Queues.AUTHENTICATION_REQUEST.getName());
        BrokerMessageDestination authenticationResponseQueue = broker.registerQueue(Queues.AUTHENTICATION_RESPONSE.getName());
        BrokerMessageDestination accountCreationConfirmationRequestQueue = broker.registerQueue(Queues.CREATE_ACCOUNT_CONFIRMATION_REQUEST.getName());

        BrokerMessageProducer tempProducer = temporaryAccountCreatedQueue.createProducer();
        BrokerMessageProducer authenticationResponseProducer = authenticationResponseQueue.createProducer();

        accountCreationRequestQueue.createConsumer(message -> {
            this.logMessage(Queues.CREATE_ACCOUNT_REQUEST, message);
            try {
                TemporaryAccountCreationResultDto result = accountCreationManager.create(this.from(message.getText()));
                tempProducer.sendMessage(TemporaryAccountResultMapper.getInstance().to(result), BrokerMessageHeader.correlationId(message.getCorrelationId()));
            } catch (TechnicalException e) {
                this.logException(Queues.CREATE_ACCOUNT_REQUEST, e);
            }
        });

        accountCreationConfirmationRequestQueue.createConsumer(message -> {
            this.logMessage(Queues.CREATE_ACCOUNT_CONFIRMATION_REQUEST, message);
            try {
                accountCreationManager.confirmAccount(AccountConfirmationMapper.getInstance().from(message.getText()));
            } catch (TechnicalException e) {
                this.logException(Queues.CREATE_ACCOUNT_CONFIRMATION_REQUEST, e);
            }
        });

        authenticationRequestQueue.createConsumer(message -> {
            this.logMessage(Queues.AUTHENTICATION_REQUEST, message);
            try {
                Credentials r = CredentialsMapper.getInstance().from(message.getText());
                Token token = authenticationManager.authenticate(r);
                this.logger.log(System.Logger.Level.DEBUG, "Send authentication response message to {} : {}", token.getId(), token.getStatus());
                authenticationResponseProducer.sendMessage(TokenMapper.getInstance().to(token), BrokerMessageHeader.correlationId(message.getCorrelationId()));
            } catch (TechnicalException e) {
                this.logException(Queues.AUTHENTICATION_REQUEST, e);
            }
        });
    }

    private void logMessage(Queues queue, BrokerMessage message) {
        this.logger.log(System.Logger.Level.DEBUG, "message received in {}: {}", queue.getName(), message.getText());
    }

    private void logException(Queues queue, TechnicalException e) {
        this.logger.log(System.Logger.Level.WARNING, "Unexpected message in {}", queue.getName(), e);
    }

    private TemporaryAccountDto from(String s) {
        try {
            String[] v = s.split("@@");
            return new TemporaryAccountDto(v[0], v[1], v[2], v[3]);
        } catch (IndexOutOfBoundsException var3) {
            throw new IllegalArgumentException(var3);
        }
    }

}
