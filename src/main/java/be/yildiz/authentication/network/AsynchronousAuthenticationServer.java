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

import be.yildiz.authentication.AccountCreationManager;
import be.yildiz.authentication.AuthenticationManager;
import be.yildiz.module.messaging.Broker;
import be.yildiz.module.messaging.BrokerMessageDestination;
import be.yildiz.module.messaging.Header;
import be.yildiz.module.messaging.JmsMessageProducer;
import be.yildizgames.common.authentication.AuthenticationChecker;
import be.yildizgames.common.authentication.Credentials;
import be.yildizgames.common.authentication.TemporaryAccountValidationException;
import be.yildizgames.common.authentication.Token;
import be.yildizgames.common.authentication.protocol.TemporaryAccountCreationResultDto;
import be.yildizgames.common.authentication.protocol.mapper.CredentialsMapper;
import be.yildizgames.common.authentication.protocol.mapper.TemporaryAccountMapper;
import be.yildizgames.common.authentication.protocol.mapper.TemporaryAccountResultMapper;
import be.yildizgames.common.authentication.protocol.mapper.TokenMapper;
import be.yildizgames.common.logging.LogFactory;
import be.yildizgames.common.mapping.MappingException;
import org.slf4j.Logger;

/**
 * @author Grégory Van den Borre
 */
public class AsynchronousAuthenticationServer {

    private final Logger logger = LogFactory.getInstance().getLogger(this.getClass());

    public AsynchronousAuthenticationServer(Broker broker, AccountCreationManager accountCreationManager, AuthenticationManager authenticationManager) {
        BrokerMessageDestination temporaryAccountCreatedQueue = broker.registerQueue("authentication-creation-temporary");
        BrokerMessageDestination accountCreationRequestQueue = broker.registerQueue("create-account-request");
        BrokerMessageDestination authenticationRequestQueue = broker.registerQueue("authentication-request");
        BrokerMessageDestination authenticationResponseQueue = broker.registerQueue("authentication-response");
        JmsMessageProducer tempProducer = temporaryAccountCreatedQueue.createProducer();
        accountCreationRequestQueue.createConsumer((message) -> {
            try {
                TemporaryAccountCreationResultDto result = accountCreationManager.create(TemporaryAccountMapper.getInstance().from(message.getText()));
                tempProducer.sendMessage(TemporaryAccountResultMapper.getInstance().to(result), Header.correlationId(message.getCorrelationId()));
            } catch (MappingException e) {
                logger.warn("Unexpected message", e);
            } catch (TemporaryAccountValidationException e) {
                TemporaryAccountCreationResultDto result = new TemporaryAccountCreationResultDto();
                for(AuthenticationChecker.AuthenticationError error: e.getExceptions()) {
                    if(error == AuthenticationChecker.AuthenticationError.LOGIN_TOO_LONG) {
                        result.setInvalidLogin(true);
                    } else if(error == AuthenticationChecker.AuthenticationError.LOGIN_TOO_SHORT) {
                        result.setInvalidLogin(true);
                    } else if(error == AuthenticationChecker.AuthenticationError.INVALID_LOGIN_CHAR) {
                        result.setInvalidLogin(true);
                    } else if(error == AuthenticationChecker.AuthenticationError.PASS_TOO_SHORT) {
                        result.setInvalidPassword(true);
                    } else if(error == AuthenticationChecker.AuthenticationError.PASS_TOO_LONG) {
                        result.setInvalidPassword(true);
                    } else if(error == AuthenticationChecker.AuthenticationError.INVALID_PASS_CHAR) {
                        result.setInvalidPassword(true);
                    }
                }
                tempProducer.sendMessage(TemporaryAccountResultMapper.getInstance().to(result), Header.correlationId(message.getCorrelationId()));
            }
        });
        JmsMessageProducer authenticationResponseProducer = authenticationResponseQueue.createProducer();
        authenticationRequestQueue.createConsumer((message) -> {
            try {
                Credentials r = CredentialsMapper.getInstance().from(message.getText());
                Token token = authenticationManager.authenticate(r);
                logger.debug("Send authentication response message to " + token.getId() + " : " + token.getStatus());
                authenticationResponseProducer.sendMessage(TokenMapper.getInstance().to(token), Header.correlationId(message.getCorrelationId()));
            } catch (MappingException e) {
                logger.warn("Unexpected message", e);
            }
        });
    }


}
