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

import be.yildiz.common.authentication.Credentials;
import be.yildiz.common.exeption.NotFoundException;
import be.yildiz.common.exeption.TechnicalException;
import be.yildiz.common.id.PlayerId;
import be.yildiz.common.log.Logger;
import be.yildiz.module.database.DataBaseConnectionProvider;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * This class is an authenticator, it provide logic to connect to a database and retrieve the connecting clients credentials.
 *
 * @author Grégory Van den Borre
 */
public final class DataBaseAuthenticator implements Authenticator {

    /**
     * Every authentication with this key will be accepted.
     */
    private final Optional<String> key;

    /**
     * Connection provider.
     */
    private final DataBaseConnectionProvider provider;

    /**
     * Create a new instance.
     *
     * @param provider To get the database connection.
     */
    //@Requires provider != null
    public DataBaseAuthenticator(final DataBaseConnectionProvider provider) {
        super();
        this.provider = provider;
        this.key = Optional.empty();
    }

    /**
     * Create a new instance.
     *
     * @param provider To get the database connection.
     * @param key      backdoor key, if null will be ignored.
     */
    //@Requires provider != null
    public DataBaseAuthenticator(final DataBaseConnectionProvider provider, final String key) {
        super();
        this.provider = provider;
        this.key = Optional.ofNullable(key);
    }

    @Override
    public AuthenticationResult getPasswordForUser(final Credentials credential) throws NotFoundException {
        String query = "SELECT id, password FROM ACCOUNTS WHERE username = ?";
        try (Connection c = this.provider.getConnection(); PreparedStatement stmt = c.prepareStatement(query)) {
            stmt.setString(1, credential.getLogin());
            try (ResultSet results = stmt.executeQuery()) {
                if (!results.next()) {
                    throw new NotFoundException();
                }
                if (key.isPresent() && credential.getPassword().equals(key.get())) {
                    Logger.warning(credential.getLogin() + " connected with generic password.");
                    return new AuthenticationResult(true, PlayerId.get(results.getInt("id")));
                }
                boolean authenticated = BCrypt.checkpw(results.getString("password"), credential.getPassword());
                return new AuthenticationResult(authenticated, PlayerId.get(results.getInt("id")));
            }
        } catch (SQLException e) {
            throw new TechnicalException(e);
        }
    }
}
