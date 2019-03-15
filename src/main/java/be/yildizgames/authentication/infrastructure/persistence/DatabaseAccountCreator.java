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

import be.yildizgames.authentication.application.AccountCreator;
import be.yildizgames.authentication.infrastructure.TemporaryAccountDto;
import be.yildizgames.common.authentication.BCryptEncryptionTool;
import be.yildizgames.common.authentication.EncryptionTool;
import be.yildizgames.common.authentication.protocol.AccountConfirmationDto;
import be.yildizgames.common.exception.implementation.ImplementationException;
import be.yildizgames.module.database.DataBaseConnectionProvider;
import be.yildizgames.module.database.Transaction;
import be.yildizgames.module.messaging.AsyncMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

/**
 * Materialize the account and temporary account in the database.
 * @author Grégory Van den Borre
 */
public class DatabaseAccountCreator implements AccountCreator {

    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * To connect to the database.
     */
    private final DataBaseConnectionProvider provider;

    /**
     * To send messages to the async message system.
     */
    private final AsyncMessageProducer messageProducer;

    /**
     * To encrypt the passwords.
     */
    private final EncryptionTool encryptionTool = new BCryptEncryptionTool();

    public DatabaseAccountCreator(DataBaseConnectionProvider provider, AsyncMessageProducer messageProducer) {
        super();
        ImplementationException.throwForNull(provider);
        ImplementationException.throwForNull(messageProducer);
        this.provider = provider;
        this.messageProducer = messageProducer;
    }

    @Override
    public final boolean loginAlreadyExist(String login) {
        ImplementationException.throwForNull(login);
        try (Connection c = this.provider.getConnection();
             PreparedStatement stmt = this.createPreparedStatementSearchAccount(c, login);
             ResultSet result = stmt.executeQuery()) {
            if(result.next()) {
                return true;
            }
            try (ResultSet resultTemp = this.createPreparedStatementSearchTempAccount(c, login).executeQuery()) {
                return resultTemp.next();
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    private PreparedStatement createPreparedStatementSearchAccount(Connection c, String login) throws SQLException {
        String query = "SELECT ID FROM ACCOUNTS WHERE LOGIN = ? AND ACTIVE = '1'";
        PreparedStatement stmt = c.prepareStatement(query);
        stmt.setString(1, login);
        return stmt;
    }

    private PreparedStatement createPreparedStatementSearchTempAccount(Connection c, String login) throws SQLException {
        String query = "SELECT ID FROM TEMP_ACCOUNTS WHERE LOGIN = ?";
        PreparedStatement stmt = c.prepareStatement(query);
        stmt.setString(1, login);
        return stmt;
    }

    @Override
    public final boolean emailAlreadyExist(String email) {
        ImplementationException.throwForNull(email);
        try (Connection c = this.provider.getConnection();
             PreparedStatement stmt = this.createPreparedStatementSearchEmail(c, email);
             ResultSet result = stmt.executeQuery()) {
            if(result.next()) {
                return true;
            }
            try (PreparedStatement stmtTemp = this.createPreparedStatementSearchTempEmail(c, email);
                 ResultSet resultTemp = stmtTemp.executeQuery()) {
                return resultTemp.next();
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    private PreparedStatement createPreparedStatementSearchEmail(Connection c, String email) throws SQLException {
        String query = "SELECT ID FROM ACCOUNTS WHERE EMAIL = ? AND ACTIVE = '1'";
        PreparedStatement stmt = c.prepareStatement(query);
        stmt.setString(1, email);
        return stmt;
    }

    private PreparedStatement createPreparedStatementSearchTempEmail(Connection c, String email) throws SQLException {
        String query = "SELECT ID FROM TEMP_ACCOUNTS WHERE EMAIL = ?";
        PreparedStatement stmt = c.prepareStatement(query);
        stmt.setString(1, email);
        return stmt;
    }

    @Override
    public final void create(TemporaryAccountDto dto, UUID token) {
        this.logger.debug("Create temporary account for {}.", dto.login);
        ImplementationException.throwForNull(dto);
        ImplementationException.throwForNull(token);
        String sql = "INSERT INTO TEMP_ACCOUNTS (LOGIN, PASSWORD, EMAIL, CHECK_VALUE, DATE) VALUES (?,?,?,?,?)";
        try (Connection c = this.provider.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, dto.login);
            stmt.setString(2, this.encryptionTool.encrypt(dto.password));
            stmt.setString(3, dto.email);
            stmt.setString(4, token.toString());
            stmt.setTimestamp(5, Timestamp.from(Instant.now()));
            stmt.executeUpdate();
            this.logger.debug("Create temporary account for {} successfully executed.", dto.login);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void confirm(AccountConfirmationDto validation) {
        ImplementationException.throwForNull(validation);
        Transaction transaction = new Transaction(this.provider);
        transaction.execute(c -> {
            String query = "SELECT * FROM TEMP_ACCOUNTS WHERE LOGIN = ?";
            PreparedStatement getTemp = c.prepareStatement(query);
            getTemp.setString(1, validation.getLogin());
            ResultSet rs = getTemp.executeQuery();
            if (!rs.next()) {
                this.logger.warn("Invalid login received from {}", validation.getLogin());
                return;
            }
            int id = rs.getInt(1);
            String login = rs.getString(2);
            String password = rs.getString(3);
            String email = rs.getString(4);
            String token = rs.getString(5);

            if (!token.equals(validation.getToken())) {
                this.logger.warn("Invalid token received from {}", login);
                return;
            }

            insertAccount(c, login, password, email);
            deleteTemp(c, id);
            int accountId = getCreatedAccountId(c, login);
            this.messageProducer.sendMessage("{login:" + login + ", id:" + accountId + "}");
        });
    }

    private void insertAccount(Connection c, String login, String password, String email) throws SQLException{
        String query = "INSERT INTO ACCOUNTS (LOGIN, PASSWORD, EMAIL, ACTIVE) VALUES (?,?,?,?)";
        try(PreparedStatement insertAccount = c.prepareStatement(query)) {
            insertAccount.setString(1, login);
            insertAccount.setString(2, password);
            insertAccount.setString(3, email);
            insertAccount.setBoolean(4, true);
            insertAccount.executeUpdate();
        }
    }

    private void deleteTemp(Connection c, int id) throws SQLException{
        String query = "DELETE FROM TEMP_ACCOUNTS WHERE ID = ?";
        try(PreparedStatement deleteTemp = c.prepareStatement(query)) {
            deleteTemp.setInt(1, id);
            deleteTemp.executeUpdate();
        }
    }

    private int getCreatedAccountId(Connection c, String login) throws SQLException{
        int accountId;
        try(PreparedStatement getAccount = createPreparedStatementAccountId(c, login);
            ResultSet rs = getAccount.executeQuery()) {
            rs.next();
            accountId = rs.getInt(1);
        }
        return accountId;
    }

    private PreparedStatement createPreparedStatementAccountId(Connection c, String login) throws SQLException {
        String query = "SELECT ID FROM ACCOUNTS WHERE LOGIN = ?";
        PreparedStatement stmt = c.prepareStatement(query);
        stmt.setString(1, login);
        return stmt;
    }
}
