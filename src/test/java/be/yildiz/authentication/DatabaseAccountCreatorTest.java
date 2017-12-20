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
import be.yildiz.module.database.TestingDatabaseInit;
import be.yildizgames.common.authentication.protocol.AccountValidationDto;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Grégory Van den Borre
 */
@Disabled
@Tag("database")
class DatabaseAccountCreatorTest {

    @Nested
    class LoginAlreadyExists {

        private DataBaseConnectionProvider givenAConnexionProvider() throws Exception {
            Thread.sleep(500);
            return new TestingDatabaseInit().init("test_db.xml");
        }

        @Test
        void doesNotExistInAccountNorInTemp() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> {});
                Assertions.assertFalse(creator.loginAlreadyExist("notexisting"));
            }
        }

        @Test
        void existsInAccountNotInTemp() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> {});
                Assertions.assertTrue(creator.loginAlreadyExist("existing"));
            }
        }

        @Test
        void existsInTempNotInAccount() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> {});
                Assertions.assertTrue(creator.loginAlreadyExist("existingTemp"));
            }
        }

        @Test
        void technicalException() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> {});
                dbcp.close();
                Assertions.assertThrows(TechnicalException.class, () -> creator.loginAlreadyExist("test"));
            }
        }

        @Test
        void fromNull() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> {});
                Assertions.assertThrows(AssertionError.class, () -> creator.loginAlreadyExist(null));
            }
        }
    }

    @Nested
    class EmailAlreadyExists {

        private DataBaseConnectionProvider givenAConnexionProvider() throws Exception {
            Thread.sleep(500);
            return new TestingDatabaseInit().init("test_db.xml");
        }

        @Test
        void doesNotExistInAccountNorInTemp() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> {});
                Assertions.assertFalse(creator.emailAlreadyExist("notexisting@me.com"));
            }
        }

        @Test
        void existsInAccountNotInTemp() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> {});
                Assertions.assertTrue(creator.emailAlreadyExist("existing@e.com"));
            }
        }

        @Test
        void existsInTempNotInAccount() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> {});
                Assertions.assertTrue(creator.emailAlreadyExist("existingTemp@e.com"));
            }
        }

        @Test
        void technicalException() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> {});
                dbcp.close();
                Assertions.assertThrows(TechnicalException.class, () -> creator.emailAlreadyExist("test@test.com"));
            }
        }
    }

    @Nested
    class Validate {

        private DataBaseConnectionProvider givenAConnexionProvider() throws Exception {
            Thread.sleep(500);
            return new TestingDatabaseInit().init("test_db.xml");
        }

        @Test
        void happyFlow() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                Result message = new Result();
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> message.value = m);
                creator.validate(givenAnAccountValidationDto());
                Assertions.assertEquals("{login:existingTemp, id:4}", message.value);
                try (Connection c = dbcp.getConnection();
                     PreparedStatement stmt = c.prepareStatement("SELECT * FROM TEMP_ACCOUNTS WHERE LOGIN = 'existingTemp'");
                     ResultSet resultSet = stmt.executeQuery()) {
                    Assertions.assertFalse(resultSet.next());
                }
                try (Connection c = dbcp.getConnection();
                     PreparedStatement stmt = c.prepareStatement("SELECT * FROM ACCOUNTS WHERE LOGIN = 'existingTemp'");
                     ResultSet resultSet = stmt.executeQuery()) {
                    Assertions.assertTrue(resultSet.next());
                }
            }
        }

        @Test
        void validationFails() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                Result message = new Result();
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> message.value = m);
                creator.validate(givenAWrongAccountValidationDto());
                Assertions.assertEquals("", message.value);
                try (Connection c = dbcp.getConnection();
                     PreparedStatement stmt = c.prepareStatement("SELECT * FROM TEMP_ACCOUNTS WHERE LOGIN = 'existingTemp'");
                     ResultSet resultSet = stmt.executeQuery()) {
                    Assertions.assertTrue(resultSet.next());
                }
                try (Connection c = dbcp.getConnection();
                     PreparedStatement stmt = c.prepareStatement("SELECT * FROM ACCOUNTS WHERE LOGIN = 'existingTemp'");
                     ResultSet resultSet = stmt.executeQuery()) {
                    Assertions.assertFalse(resultSet.next());
                }
            }
        }

        @Test
        void accountNotExisting() throws Exception {
            //TODO check for rollback
        }

        @Test
        void insertionFails() throws Exception {
            //TODO check for rollback
        }

        @Test
        void deleteFails() throws Exception {
            //TODO check for rollback
        }

        AccountValidationDto givenAnAccountValidationDto() {
            return new AccountValidationDto("existingTemp", "azerty");
        }

        AccountValidationDto givenAWrongAccountValidationDto() {
            return new AccountValidationDto("existingTemp", "123");
        }


        AccountValidationDto givenANotExistingAccountValidationDto() {
            return new AccountValidationDto("tempNotExisting", "1234");
        }

    }

    private class Result {

        private String value = "";


    }


}
