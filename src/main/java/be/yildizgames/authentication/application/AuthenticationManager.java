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
 */

package be.yildizgames.authentication.application;

import be.yildizgames.common.authentication.AuthenticationChecker;
import be.yildizgames.common.authentication.AuthenticationRules;
import be.yildizgames.common.authentication.CredentialException;
import be.yildizgames.common.authentication.Credentials;
import be.yildizgames.common.authentication.SimpleAuthenticationChecker;
import be.yildizgames.common.authentication.Token;
import be.yildizgames.common.authentication.UserNotFoundException;
import be.yildizgames.common.authentication.protocol.TokenVerification;
import be.yildizgames.common.model.PlayerId;
import be.yildizgames.common.util.Util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class handling the business logic about the authentication, it will manage the authentication itself, banned players, number of allowed failed authentications...
 *
 * @author Grégory Van den Borre
 */
public class AuthenticationManager {

    /**
     * Time, in milliseconds to wait before being able to try to authenticate again after some failure.
     */
    private static final Duration AUTHENTICATION_FAILURE_BAN_TIME = Duration.ofSeconds(15);

    /**
     * Number of maximum authentication failure before being banned for a small amount of time.
     */
    private static final int AUTHENTICATION_MAXIMUM_FAILURE = 5;

    /**
     * List of successfully setAuthenticated players.
     */
    private final Map<PlayerId, Token> authenticatedPlayers = new HashMap<>();

    /**
     * Check if the inputs are correct following a given set of rules.
     */
    private final AuthenticationChecker checker = new SimpleAuthenticationChecker(AuthenticationRules.DEFAULT);

    /**
     * List of failed authentications, the key is the player's name and the value is the number of failure.
     */
    private final Map<String, Integer> failedAuthentication = new HashMap<>();

    /**
     * List of banned players, the key is the player's name, and the value the time when the ban is removed.
     */
    private final Map<String, LocalDateTime> banned = new HashMap<>();

    /**
     * Provide the authentication logic.
     */
    private final Authenticator authenticator;

    public AuthenticationManager(Authenticator authenticator) {
        Objects.requireNonNull(authenticator);
        this.authenticator = authenticator;
    }

    /**
     * Check if the authentication request is valid and notify the listeners if it is. If the authentication fails, a value is incremented for the player trying to connect, if the value reaches 5, the
     * player will not be able to connect for 5 minutes.
     *
     * @param auth Received authentication data.
     * @return A token with the authentication state.
     * @throws NullPointerException If request is null.
     */
    public final Token authenticate(final Credentials auth) {
        this.checkIfToBeBanned(auth.login);
        Token token;
        if (this.isBanned(auth.login)) {
            this.resetConnectionFailure(auth.login);
            token = Token.banned();
        } else {
            try {
                TokenVerification result = this.authenticator.getPasswordForUser(this.checker.check(auth.login, auth.password));
                this.addConnectionFailure(auth.login);
                if (result.authenticated) {
                    token = this.authenticatedPlayers.getOrDefault(result.userId, this.setAuthenticated(auth.login, result.userId));
                } else {
                    token = Token.authenticationFailed();
                }
            } catch (UserNotFoundException | CredentialException e) {
                token = Token.notFound();
            }
        }
        return token;
    }

    /**
     * Retrieve a token for a given player, if the player is not found, a not found token is returned.
     *
     * @param id Player id.
     * @return A token for tha player, or not found if nothing is matching.
     */
    public final Token getAuthenticated(final PlayerId id) {
        return this.authenticatedPlayers.getOrDefault(id, Token.notFound());
    }

    /**
     * Set a player as setAuthenticated, reset its connections failures and provide the proper token.
     *
     * @param login Login of the authenticated user.
     * @param id Id of the authenticated user.
     * @return The authentication token for the user.
     */
    private Token setAuthenticated(final String login, final PlayerId id) {
        int key = Util.getRandom();
        Token token = Token.authenticated(id, System.currentTimeMillis(), key);
        this.authenticatedPlayers.put(id, token);
        this.resetConnectionFailure(login);
        return token;
    }

    /**
     * Check if a player is banned or not.
     *
     * @param login Login of the player to check.
     * @return <code>true</code> if the player is banned.
     */
    private boolean isBanned(final String login) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bannedTime = this.banned.getOrDefault(login, now);
        return bannedTime.isAfter(now);
    }

    /**
     * Check if a player has too many time entered a wrong password,
     * if so it will be added to the banned list for a limited amount of time.
     *
     * @param login Login of the player.
     */
    private void checkIfToBeBanned(final String login) {
        if (this.failedAuthentication.getOrDefault(login, 0).equals(AUTHENTICATION_MAXIMUM_FAILURE)) {
            this.banned.put(login, LocalDateTime.now().plus(AUTHENTICATION_FAILURE_BAN_TIME));
        }
    }

    /**
     * Increase the number of authentication failure for a player.
     *
     * @param login Login of the player.
     */
    private void addConnectionFailure(final String login) {
        int value = this.failedAuthentication.getOrDefault(login, 0);
        value++;
        this.failedAuthentication.put(login, value);
    }

    /**
     * Reset to 0 the number of authentication failure for a player.
     *
     * @param name Name of the player.
     */
    private void resetConnectionFailure(final String name) {
        this.failedAuthentication.put(name, 0);
    }

}
