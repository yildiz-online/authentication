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

package be.yildizgames.authentication.application;

import be.yildizgames.authentication.configuration.EmailTemplateConfiguration;
import be.yildizgames.authentication.infrastructure.TemporaryAccountDto;
import be.yildizgames.authentication.infrastructure.io.mail.EmailService;
import be.yildizgames.common.authentication.AuthenticationError;
import be.yildizgames.common.authentication.TemporaryAccount;
import be.yildizgames.common.authentication.TemporaryAccountValidationException;
import be.yildizgames.common.authentication.protocol.AccountConfirmationDto;
import be.yildizgames.common.authentication.protocol.TemporaryAccountCreationResultDto;
import be.yildizgames.common.exception.implementation.ImplementationException;
import be.yildizgames.common.exception.technical.TechnicalException;

import java.util.List;
import java.util.UUID;

/**
 * @author Grégory Van den Borre
 */
public class AccountCreationManager {

    /**
     * Logger.
     */
    private final System.Logger logger = System.getLogger(this.getClass().toString());

    /**
     * To materialize the account.
     */
    private final AccountCreator accountCreator;

    /**
     * To send emails.
     */
    private final EmailService emailService;

    /**
     * Template of the email to send.
     */
    private final EmailTemplateConfiguration configuration;

    /**
     * Create a new instance.
     * @param accountCreator To materialize the account.
     * @param emailService To send emails.
     * @param configuration Template of the email to send.
     */
    public AccountCreationManager(final AccountCreator accountCreator, final EmailService emailService, final EmailTemplateConfiguration configuration) {
        super();
        ImplementationException.throwForNull(accountCreator);
        ImplementationException.throwForNull(emailService);
        ImplementationException.throwForNull(configuration);
        this.accountCreator = accountCreator;
        this.emailService = emailService;
        this.configuration = configuration;
    }

    /**
     * Create and persist a temporary account.
     * @param dto Temporary account to build.
     * @return The created temporary account, waiting to be validated.
     */
    public final TemporaryAccountCreationResultDto create(TemporaryAccountDto dto) {
        this.logger.log(System.Logger.Level.DEBUG, "Prepare creation of the temporary account for {}.", dto.login);
        TemporaryAccountCreationResultDto result = TemporaryAccountCreationResultDto.success();
        try {
            TemporaryAccount.create(dto.login, dto.password, dto.email, dto.language);
        } catch (TemporaryAccountValidationException e) {
            this.logger.log(System.Logger.Level.DEBUG, "Validation error for the temporary account for {}.", dto.login);
            this.handleTemporaryAccountValidationError(result, e.getExceptions());
            return result;
        }
        UUID token = UUID.randomUUID();
        try {
            if (this.accountCreator.emailAlreadyExist(dto.email)) {
                result.setEmailExisting(true);
                this.logger.log(System.Logger.Level.DEBUG, "Account for {} not created, email already exists.", dto.login);
            }
            if (this.accountCreator.loginAlreadyExist(dto.login)) {
                result.setAccountExisting(true);
                this.logger.log(System.Logger.Level.DEBUG, "Account for {} not created, account already exists.", dto.login);
            }
            if (!result.hasError()) {
                this.accountCreator.create(dto, token);
                this.emailService.send(new TemporaryAccountEmail(this.configuration.getEmailTemplatePath(dto.language), dto.login, dto.email, token.toString()));
            }
        } catch (TechnicalException e) {
            this.logger.log(System.Logger.Level.ERROR, "Error while persisting temp account {} : {}", dto, token, e);
            result.setTechnicalIssue(true);
        }
        return result;
    }

    /**
     * Confirm a temporary account.
     * @param validation Dto containing the confirmation data.
     */
    public void confirmAccount(final AccountConfirmationDto validation) {
        this.accountCreator.confirm(validation);
    }

    /**
     * Fill the temporary account validation errors
     * @param result Result to fill.
     * @param exceptions Validation errors.
     */
    private void handleTemporaryAccountValidationError(TemporaryAccountCreationResultDto result, List<AuthenticationError> exceptions) {
        for (AuthenticationError error : exceptions) {
            if (error == AuthenticationError.LOGIN_TOO_LONG) {
                result.setInvalidLogin(true);
            } else if (error == AuthenticationError.LOGIN_TOO_SHORT) {
                result.setInvalidLogin(true);
            } else if (error == AuthenticationError.LOGIN_EMPTY) {
                result.setInvalidLogin(true);
            } else if (error == AuthenticationError.INVALID_LOGIN_CHAR) {
                result.setInvalidLogin(true);
            } else if (error == AuthenticationError.PASS_EMPTY) {
                result.setInvalidPassword(true);
            } else if (error == AuthenticationError.PASS_TOO_SHORT) {
                result.setInvalidPassword(true);
            } else if (error == AuthenticationError.PASS_TOO_LONG) {
                result.setInvalidPassword(true);
            } else if (error == AuthenticationError.INVALID_PASS_CHAR) {
                result.setInvalidPassword(true);
            } else if (error == AuthenticationError.MAIL_EMPTY) {
                result.setEmailMissing(true);
            } else if (error == AuthenticationError.MAIL_INVALID) {
                result.setEmailInvalid(true);
            }
        }
    }
}
