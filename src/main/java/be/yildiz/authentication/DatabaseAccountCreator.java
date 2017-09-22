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

import be.yildiz.common.exeption.TechnicalException;
import be.yildiz.module.database.DataBaseConnectionProvider;
import be.yildiz.module.database.Transaction;
import be.yildiz.module.messaging.MessageProducer;
import be.yildiz.module.network.protocol.AccountValidationDto;
import be.yildiz.module.network.protocol.TemporaryAccountDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Grégory Van den Borre
 */
public class DatabaseAccountCreator implements AccountCreator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DataBaseConnectionProvider provider;

    private final MessageProducer messageProducer;

    public DatabaseAccountCreator(DataBaseConnectionProvider provider, MessageProducer messageProducer) {
        this.provider = provider;
        this.messageProducer = messageProducer;
    }

    @Override
    public boolean loginAlreadyExist(String login) {
        assert login != null;
        try (Connection c = this.provider.getConnection();
             ResultSet result = this.createPreparedStatementSearchAccount(c, login).executeQuery()) {
            if(result.first()) {
                return true;
            }
            try (ResultSet resultTemp = this.createPreparedStatementSearchTempAccount(c, login).executeQuery()) {
                return resultTemp.first();
            }
        } catch (SQLException e) {
            throw new TechnicalException(e);
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
    public boolean emailAlreadyExist(String email) {
        assert email != null;
        try (Connection c = this.provider.getConnection();
             ResultSet result = this.createPreparedStatementSearchEmail(c, email).executeQuery()) {
            if(result.first()) {
                return true;
            }
            try (ResultSet resultTemp = this.createPreparedStatementSearchTempEmail(c, email).executeQuery()) {
                return resultTemp.first();
            }
        } catch (SQLException e) {
            throw new TechnicalException(e);
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
    public void create(TemporaryAccountDto dto, UUID token) {
        assert dto != null;
        String sql = "INSERT INTO TEMP_ACCOUNTS (LOGIN, PASSWORD, EMAIL, CHECK_VALUE, DATE) VALUES (?,?,?,?,?)";
        try (Connection c = this.provider.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, dto.getLogin());
            stmt.setString(2, dto.getPassword());
            stmt.setString(3, dto.getEmail());
            stmt.setString(4, token.toString());
            stmt.setTimestamp(5, Timestamp.from(Instant.now()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new TechnicalException(e);
        }
    }

    @Override
    public void validate(AccountValidationDto validation) {
        Transaction transaction = new Transaction(this.provider);
        transaction.execute((c) -> {
            String query = "SELECT * FROM TEMP_ACCOUNTS WHERE LOGIN = ?";
            PreparedStatement getTemp = c.prepareStatement(query);
            getTemp.setString(1, validation.getLogin());
            ResultSet rs = getTemp.executeQuery();
            rs.first();
            int id = rs.getInt(1);
            String login = rs.getString(2);
            String password = rs.getString(3);
            String email = rs.getString(4);
            String token = rs.getString(5);

            if (!token.equals(validation.getToken())) {
                logger.warn("Invalid token received from " + login);
                return;
            }

            insertAccount(c, login, password, email);
            deleteTemp(c, id);
            int accountId = getCreatedAccountId(c, login);
            messageProducer.sendMessage("{login:" + login + ", id:" + accountId + "}");
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
        String query = "SELECT ID FROM ACCOUNTS WHERE LOGIN = ?";
        int accountId;
        try(PreparedStatement getAccount = c.prepareStatement(query)) {
            getAccount.setString(1, login);
            ResultSet rs = getAccount.executeQuery();
            rs.first();
            accountId = rs.getInt(1);
        }
        return accountId;
    }
}
