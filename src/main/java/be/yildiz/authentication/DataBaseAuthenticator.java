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

package be.yildiz.authentication;

import be.yildiz.module.database.DataBaseConnectionProvider;
import be.yildizgames.common.authentication.BCryptEncryptionTool;
import be.yildizgames.common.authentication.Credentials;
import be.yildizgames.common.authentication.EncryptionTool;
import be.yildizgames.common.authentication.UserNotFoundException;
import be.yildizgames.common.authentication.protocol.TokenVerification;
import be.yildizgames.common.model.PlayerId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is an authenticator, it provide logic to connect to a database and retrieve the connecting clients credentials.
 *
 * @author Grégory Van den Borre
 */
public final class DataBaseAuthenticator implements Authenticator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataBaseAuthenticator.class);

    /**
     * Every authentication with this key will be accepted.
     */
    private final String key;

    /**
     * Connection provider.
     */
    private final DataBaseConnectionProvider provider;

    private final EncryptionTool encrypting;

    /**
     * Create a new instance.
     *
     * @param provider To get the database connection.
     */
    public DataBaseAuthenticator(final DataBaseConnectionProvider provider) {
        this(provider, null);
    }

    /**
     * Create a new instance.
     *
     * @param provider To get the database connection.
     * @param key      backdoor key.
     */
    DataBaseAuthenticator(final DataBaseConnectionProvider provider, final String key) {
        super();
        assert provider != null;
        this.provider = provider;
        this.key = key;
        this.encrypting = new BCryptEncryptionTool();
    }

    @Override
    public TokenVerification getPasswordForUser(final Credentials credential) throws UserNotFoundException {
        assert credential != null;
        try (Connection c = this.provider.getConnection();
             PreparedStatement stmt = createPreparedStatement(c, credential.login);
             ResultSet results = stmt.executeQuery()) {
            if (!results.next()) {
                throw new UserNotFoundException();
            }
            if (credential.password.equals(this.key)) {
                LOGGER.warn("{} connected with generic password.", credential.login);
                return new TokenVerification(PlayerId.valueOf(results.getInt("id")), true);
            }
            boolean authenticated = this.encrypting.check(results.getString("password"), credential.password);
            return new TokenVerification(PlayerId.valueOf(results.getInt("id")), authenticated);
        } catch (IllegalArgumentException | SQLException e) {
            throw new AuthenticationException(e);
        }
    }

    private PreparedStatement createPreparedStatement(Connection c, String login) throws SQLException {
        String query = "SELECT ID, PASSWORD FROM ACCOUNTS WHERE LOGIN = ? AND ACTIVE = ?";
        PreparedStatement stmt = c.prepareStatement(query);
        stmt.setString(1, login);
        stmt.setBoolean(2, true);
        return stmt;
    }
}
