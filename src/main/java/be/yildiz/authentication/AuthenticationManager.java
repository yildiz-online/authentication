//        This file is part of the Yildiz-Online project, licenced under the MIT License
//        (MIT)
//
//        Copyright (c) 2016 Grégory Van den Borre
//
//        More infos available: http://yildiz.bitbucket.org
//
//        Permission is hereby granted, free of charge, to any person obtaining a copy
//        of this software and associated documentation files (the "Software"), to deal
//        in the Software without restriction, including without limitation the rights
//        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//        copies of the Software, and to permit persons to whom the Software is
//        furnished to do so, subject to the following conditions:
//
//        The above copyright notice and this permission notice shall be included in all
//        copies or substantial portions of the Software.
//
//        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//        SOFTWARE.

package be.yildiz.authentication;

import be.yildiz.authentication.Authenticator.AuthenticationResult;
import be.yildiz.common.Token;
import be.yildiz.common.authentication.AuthenticationChecker;
import be.yildiz.common.authentication.AuthenticationRules;
import be.yildiz.common.authentication.CredentialException;
import be.yildiz.common.collections.Maps;
import be.yildiz.common.exeption.NotFoundException;
import be.yildiz.common.id.PlayerId;
import be.yildiz.common.util.Time;
import be.yildiz.common.util.Util;
import be.yildiz.module.network.protocol.AuthenticationRequest;

import java.util.Map;

/**
 * Class handling the business logic about the authentication, it will manage the authentication itself, banned players, number of allowed failed authentications...
 *
 * @author Grégory Van den Borre
 */
public class AuthenticationManager {

    /**
     * Time, in milliseconds to wait before being able to try to authenticate again after some failure.
     */
    private static final Time AUTHENTICATION_FAILURE_BAN_TIME = Time.seconds(15);

    /**
     * Number of maximum authentication failure before being banned for a small amount of time.
     */
    private static final int AUTHENTICATION_MAXIMUM_FAILURE = 5;

    /**
     * List of successfully setAuthenticated players.
     */
    private final Map<PlayerId, Token> authenticatedPlayers = Maps.newMap();

    /**
     * Check if the inputs are correct following a given set of rules.
     */
    private final AuthenticationChecker checker = new AuthenticationChecker(AuthenticationRules.DEFAULT);

    /**
     * List of failed authentications, the key is the player's name and the value is the number of failure.
     */
    private final Map<String, Integer> failedAuthentication = Maps.newMap();

    /**
     * List of banned players, the key is the player's name, and the value the time when the ban is removed.
     */
    private final Map<String, Time> banned = Maps.newMap();

    /**
     * Provide the authentication logic.
     */
    private final Authenticator authenticator;

    public AuthenticationManager(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    /**
     * Check if the authentication request is valid and notify the listeners if it is. If the authentication fails, a value is incremented for the player trying to connect, if the value reaches 5, the
     * player will not be able to connect for 5 minutes.
     *
     * @param request Received AuthenticationRequest.
     * @return A token with the authentication state.
     * @throws NullPointerException If request is null.
     */
    public final Token authenticate(final AuthenticationRequest request) {
        this.checkIfToBeBanned(request.getLogin());
        Token token;
        if (this.isBanned(request.getLogin())) {
            this.resetConnectionFailure(request.getLogin());
            token = Token.banned();
        } else {
            try {
                AuthenticationResult result = this.authenticator.getPasswordForUser(this.checker.check(request.getLogin(), request.getPassword()));
                this.addConnectionFailure(request.getLogin());
                if (result.authenticated) {
                    token = this.authenticatedPlayers.getOrDefault(result.playerId, this.setAuthenticated(request.getLogin(), result.playerId));
                } else {
                    token = Token.authenticationFailed();
                }
            } catch (NotFoundException | CredentialException e) {
                token = Token.notFound();
            }
        }
        return token;
    }

    /**
     * Retrieve a token for a given player, if the player is not found, a not found token is returned.
     *
     * @param id Player id.
     * @return A token for tha player.
     */
    //@Requires id != null
    //@Ensures return a valid token or not found if the id does not match any registered player
    public final Token getAuthenticated(final PlayerId id) {
        return this.authenticatedPlayers.getOrDefault(id, Token.notFound());
    }

    /**
     * Set a player as setAuthenticated, reset its connections failures and provide the proper token.
     *
     * @param login
     * @param id
     * @return
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
        Time bannedTime = this.banned.getOrDefault(login, Time.ZERO);
        return bannedTime.isNotElapsed();
    }

    /**
     * Check if a player has too many time entered a wrong password, if so it will be added to the banned list for a limited amount of time.
     *
     * @param login Login of the player.
     * @requires login != null
     * @ensures if(this.failedAuthentication.getOr(login, 0) == AUTHENTICATION_MAXIMUM_FAILURE) {this.banned.contains(login)}
     */
    private void checkIfToBeBanned(final String login) {
        if (this.failedAuthentication.getOrDefault(login, 0).equals(AUTHENTICATION_MAXIMUM_FAILURE)) {
            this.banned.put(login, AUTHENTICATION_FAILURE_BAN_TIME.addNow());
        }
    }

    /**
     * Increase the number of authentication failure for a player.
     *
     * @param login Login of the player.
     */
    //@requires login != null
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
    //@requires name != null
    //@ensures failedAuthentication.get(name) == 0
    private void resetConnectionFailure(final String name) {
        this.failedAuthentication.put(name, 0);
    }
}
