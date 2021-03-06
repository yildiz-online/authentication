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

package be.yildizgames.authentication.infrastructure.persistence;

import be.yildizgames.authentication.application.Authenticator;
import be.yildizgames.common.authentication.BCryptEncryptionTool;
import be.yildizgames.common.authentication.Credentials;
import be.yildizgames.common.authentication.EncryptionTool;
import be.yildizgames.common.authentication.UserNotFoundException;
import be.yildizgames.common.authentication.protocol.TokenVerification;
import be.yildizgames.common.logging.Logger;
import be.yildizgames.common.model.PlayerId;
import be.yildizgames.module.database.DataBaseConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * This class is an authenticator, it provide logic to connect to a database and retrieve the connecting clients credentials.
 *
 * @author Grégory Van den Borre
 */
public final class DataBaseAuthenticator implements Authenticator {

    /**
     * Logger.
     */
    private final Logger logger = Logger.getLogger(this);

    /**
     * Every authentication with this key will be accepted.
     */
    private final String key;

    /**
     * Connection provider.
     */
    private final DataBaseConnectionProvider provider;

    /**
     * To encrypt the password.
     */
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
     * @param key Backdoor key, can be null.
     */
    DataBaseAuthenticator(final DataBaseConnectionProvider provider, final String key) {
        super();
        Objects.requireNonNull(provider);
        this.provider = provider;
        this.key = key;
        this.encrypting = new BCryptEncryptionTool();
    }

    @Override
    public TokenVerification getPasswordForUser(final Credentials credential) throws UserNotFoundException {
        Objects.requireNonNull(credential);
        try (Connection c = this.provider.getConnection();
             PreparedStatement stmt = createPreparedStatement(c, credential.login);
             ResultSet results = stmt.executeQuery()) {
            if (!results.next()) {
                throw new UserNotFoundException();
            }
            if (credential.password.equals(this.key)) {
                this.logger.warning("{0} connected with generic password.", credential.login);
                return new TokenVerification(PlayerId.valueOf(results.getInt("id")), true);
            }
            boolean authenticated = this.encrypting.check(results.getString("password"), credential.password);
            return new TokenVerification(PlayerId.valueOf(results.getInt("id")), authenticated);
        } catch (IllegalArgumentException | SQLException e) {
            throw new PersistenceException(e);
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
