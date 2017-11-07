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

package be.yildiz.authentication;

import be.yildiz.authentication.network.EmailService;
import be.yildiz.common.authentication.AuthenticationChecker;
import be.yildiz.common.authentication.AuthenticationRules;
import be.yildiz.common.authentication.CredentialException;
import be.yildiz.common.exeption.TechnicalException;
import be.yildiz.module.network.protocol.AccountValidationDto;
import be.yildiz.module.network.protocol.TemporaryAccountCreationResultDto;
import be.yildiz.module.network.protocol.TemporaryAccountDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author Grégory Van den Borre
 */
public class AccountCreationManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationChecker checker;

    private static final String emailRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    private final Pattern emailPattern = Pattern.compile(emailRegex);

    private final AccountCreator accountCreator;

    private final EmailService emailService;

    public AccountCreationManager(AccountCreator accountCreator, AuthenticationRules rules, EmailService emailService) {
        this.accountCreator = accountCreator;
        this.checker = new AuthenticationChecker(rules);
        this.emailService = emailService;
    }

    public TemporaryAccountCreationResultDto create(TemporaryAccountDto dto) {
        TemporaryAccountCreationResultDto result = new TemporaryAccountCreationResultDto();
        try {
            this.checker.check(dto.getLogin(), dto.getPassword());
        } catch (CredentialException e) {
            for(AuthenticationChecker.AuthenticationError error: e.getErrors()) {
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
        }

        UUID token = UUID.randomUUID();
        try {
            if(dto.getEmail() == null) {
              result.setEmailMissing(true);
            }
            if(!emailPattern.matcher(dto.getEmail()).matches()) {
                result.setEmailInvalid(true);
            }
            if(accountCreator.loginAlreadyExist(dto.getLogin())) {
                result.setAccountExisting(true);
            }
            if(accountCreator.emailAlreadyExist(dto.getEmail())) {
                result.setEmailExisting(true);
            }
            result.setToken(token.toString());
            if(!result.hasError()) {
                accountCreator.create(dto, token);
                this.emailService.send(new TemporaryAccountEmail("fr", dto.getLogin(), dto.getEmail(), token.toString()));
            }
        } catch (TechnicalException e) {
            logger.error("Error while persisting temp account " + dto + ":" + token, e);
            result.setTechnicalIssue(true);
        }
        return result;
    }

    public void validateAccount(AccountValidationDto validation) {
        accountCreator.validate(validation);
    }
}
