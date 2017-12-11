/*
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 * Copyright (c) 2017 Grégory Van den Borre
 *
 * More infos available: https://www.yildiz-games.be
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT  HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE  SOFTWARE.
 */

package be.yildiz.authentication;

import be.yildizgames.common.authentication.Credentials;
import be.yildiz.common.exeption.NotFoundException;
import be.yildiz.common.exeption.TechnicalException;
import be.yildiz.module.network.protocol.TokenVerification;

/**
 * Authenticate Credential with login against their password.
 *
 * @author Grégory Van den Borre
 */
@FunctionalInterface
public interface Authenticator {

    /**
     * Check if the given login password are correct.
     *
     * @param credentials User credentials.
     * @return The authentication result with <code>true</code> if the password is correct for the userName.
     * @throws TechnicalException If something went wrong while trying to check the credentials.
     * @throws NotFoundException  If the credentials login does not exists.
     */
    //@Requires credentials != null
    //@Requires credentials to have been checked with an {@link AuthenticationChecker} to ensure no dangerous input is passed to the authenticator.
    TokenVerification getPasswordForUser(Credentials credentials) throws NotFoundException;

}
