/*
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 *  Copyright (c) 2018 Grégory Van den Borre
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

package be.yildizgames.authentication;

import be.yildizgames.authentication.configuration.EmailTemplateConfiguration;
import be.yildizgames.authentication.network.EmailService;
import be.yildizgames.common.authentication.TemporaryAccount;
import be.yildizgames.common.authentication.protocol.AccountConfirmationDto;
import be.yildizgames.common.authentication.protocol.TemporaryAccountCreationResultDto;
import be.yildizgames.common.exception.implementation.ImplementationException;
import be.yildizgames.common.exception.technical.TechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author Grégory Van den Borre
 */
public class AccountCreationManager {

    private static final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Pattern emailPattern = Pattern.compile(EMAIL_REGEX);

    private final AccountCreator accountCreator;

    private final EmailService emailService;

    private final EmailTemplateConfiguration configuration;

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
    public final TemporaryAccountCreationResultDto create(TemporaryAccount dto) {
        TemporaryAccountCreationResultDto result = TemporaryAccountCreationResultDto.success();

        UUID token = UUID.randomUUID();
        try {
            if(dto.getEmail() == null) {
              result.setEmailMissing(true);
            } else {
                if(!this.emailPattern.matcher(dto.getEmail()).matches()) {
                    result.setEmailInvalid(true);
                }
                if(this.accountCreator.emailAlreadyExist(dto.getEmail())) {
                    result.setEmailExisting(true);
                }
            }
            if(this.accountCreator.loginAlreadyExist(dto.getLogin())) {
                result.setAccountExisting(true);
            }
            if(!result.hasError()) {
                this.accountCreator.create(dto, token);
                this.emailService.send(new TemporaryAccountEmail(this.configuration.getEmailTemplatePath(dto.getLanguage()), dto.getLogin(), dto.getEmail(), token.toString()));
            }
        } catch (TechnicalException e) {
            this.logger.error("Error while persisting temp account " + dto + ":" + token, e);
            result.setTechnicalIssue(true);
        }
        return result;
    }

    public void validateAccount(AccountConfirmationDto validation) {
        this.accountCreator.validate(validation);
    }
}
