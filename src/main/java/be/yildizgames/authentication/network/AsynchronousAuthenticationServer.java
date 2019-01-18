/*
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 *  Copyright (c) 2019 Grégory Van den Borre
 *
 *  More infos available: https://engine.yildiz-games.be
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

package be.yildizgames.authentication.network;

import be.yildizgames.authentication.AccountCreationManager;
import be.yildizgames.authentication.AuthenticationManager;
import be.yildizgames.common.authentication.AuthenticationError;
import be.yildizgames.common.authentication.Credentials;
import be.yildizgames.common.authentication.TemporaryAccountValidationException;
import be.yildizgames.common.authentication.Token;
import be.yildizgames.common.authentication.protocol.TemporaryAccountCreationResultDto;
import be.yildizgames.common.authentication.protocol.mapper.CredentialsMapper;
import be.yildizgames.common.authentication.protocol.mapper.TemporaryAccountMapper;
import be.yildizgames.common.authentication.protocol.mapper.TemporaryAccountResultMapper;
import be.yildizgames.common.authentication.protocol.mapper.TokenMapper;
import be.yildizgames.common.exception.technical.TechnicalException;
import be.yildizgames.module.messaging.Broker;
import be.yildizgames.module.messaging.BrokerMessageDestination;
import be.yildizgames.module.messaging.Header;
import be.yildizgames.module.messaging.JmsMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Grégory Van den Borre
 */
public class AsynchronousAuthenticationServer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public AsynchronousAuthenticationServer(Broker broker, AccountCreationManager accountCreationManager, AuthenticationManager authenticationManager) {
        BrokerMessageDestination temporaryAccountCreatedQueue = broker.registerQueue("authentication-creation-temporary");
        BrokerMessageDestination accountCreationRequestQueue = broker.registerQueue("create-account-request");
        BrokerMessageDestination authenticationRequestQueue = broker.registerQueue("authentication-request");
        BrokerMessageDestination authenticationResponseQueue = broker.registerQueue("authentication-response");

        JmsMessageProducer tempProducer = temporaryAccountCreatedQueue.createProducer();

        accountCreationRequestQueue.createConsumer(message -> {
            try {
                TemporaryAccountCreationResultDto result = accountCreationManager.create(TemporaryAccountMapper.getInstance().from(message.getText()));
                tempProducer.sendMessage(TemporaryAccountResultMapper.getInstance().to(result), Header.correlationId(message.getCorrelationId()));
            } catch (TechnicalException e) {
                logger.warn("Unexpected message", e);
            } catch (TemporaryAccountValidationException e) {
                TemporaryAccountCreationResultDto result = new TemporaryAccountCreationResultDto();
                for(AuthenticationError error: e.getExceptions()) {
                    if(error == AuthenticationError.LOGIN_TOO_LONG) {
                        result.setInvalidLogin(true);
                    } else if(error == AuthenticationError.LOGIN_TOO_SHORT) {
                        result.setInvalidLogin(true);
                    } else if(error == AuthenticationError.INVALID_LOGIN_CHAR) {
                        result.setInvalidLogin(true);
                    } else if(error == AuthenticationError.PASS_TOO_SHORT) {
                        result.setInvalidPassword(true);
                    } else if(error == AuthenticationError.PASS_TOO_LONG) {
                        result.setInvalidPassword(true);
                    } else if(error == AuthenticationError.INVALID_PASS_CHAR) {
                        result.setInvalidPassword(true);
                    }
                }
                tempProducer.sendMessage(TemporaryAccountResultMapper.getInstance().to(result), Header.correlationId(message.getCorrelationId()));
            }
        });
        JmsMessageProducer authenticationResponseProducer = authenticationResponseQueue.createProducer();
        authenticationRequestQueue.createConsumer(message -> {
            try {
                Credentials r = CredentialsMapper.getInstance().from(message.getText());
                Token token = authenticationManager.authenticate(r);
                logger.debug("Send authentication response message to {} : {}", token.getId(), token.getStatus());
                authenticationResponseProducer.sendMessage(TokenMapper.getInstance().to(token), Header.correlationId(message.getCorrelationId()));
            } catch (TechnicalException e) {
                logger.warn("Unexpected message", e);
            }
        });
    }


}
