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

import org.junit.jupiter.api.Tag;

/**
 * @author Grégory Van den Borre
 */
@Tag("database")
public class DatabaseAccountCreatorTest {

   /* @Nested
    public class Create {
        private DataBaseConnectionProvider givenAConnexionProvider() throws Exception {
            Thread.sleep(500);
            return new TestingDatabaseInit().init("test_db.xml");
        }

        @Test
        public void happyFlow() throws Exception {
            try (DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                Result message = new Result();
                EncryptionTool encryptionTool = new BCryptEncryptionTool();
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> message.value = m);
                UUID uuid = UUID.randomUUID();
                int random = Util.getRandom();
                creator.create(new TemporaryAccountDto("login_" + random, "passwordcreated", "email@created.com", "en"), uuid);
                try (Connection c = dbcp.getConnection();
                     PreparedStatement stmt = c.prepareStatement("SELECT * FROM TEMP_ACCOUNTS WHERE LOGIN = 'login_" + random + "'");
                     ResultSet resultSet = stmt.executeQuery()) {
                    Assertions.assertTrue(resultSet.next());
                    Assertions.assertEquals("login_" + random, resultSet.getString(2));
                    Assertions.assertTrue(encryptionTool.check(resultSet.getString(3), "passwordcreated" ));
                    Assertions.assertEquals("email@created.com", resultSet.getString(4));
                    Assertions.assertEquals(uuid.toString(), resultSet.getString(5));
                }
            }
        }

        @Test
        public void sqlException() throws Exception {
            try (DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                Result message = new Result();
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> message.value = m);
                UUID uuid = UUID.randomUUID();
                Assertions.assertThrows(PersistenceException.class, () -> creator.create(new TemporaryAccountDto("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "passwordcreated", "email@created.com", "en"), uuid));
            }
        }


    }

    @Nested
    public class LoginAlreadyExists {

        private DataBaseConnectionProvider givenAConnexionProvider() throws Exception {
            Thread.sleep(500);
            return new TestingDatabaseInit().init("test_db.xml");
        }

        @Test
        public void doesNotExistInAccountNorInTemp() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> {});
                Assertions.assertFalse(creator.loginAlreadyExist("notexisting"));
            }
        }

        @Test
        public void existsInAccountNotInTemp() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> {});
                Assertions.assertTrue(creator.loginAlreadyExist("existing"));
            }
        }

        @Test
        public void existsInTempNotInAccount() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> {});
                Assertions.assertTrue(creator.loginAlreadyExist("existingTemp"));
            }
        }

        @Test
        public void fromNull() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> {});
                Assertions.assertThrows(ImplementationException.class, () -> creator.loginAlreadyExist(null));
            }
        }
    }

    @Nested
    public class EmailAlreadyExists {

        private DataBaseConnectionProvider givenAConnexionProvider() throws Exception {
            Thread.sleep(500);
            return new TestingDatabaseInit().init("test_db.xml");
        }

        @Test
        public void doesNotExistInAccountNorInTemp() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> {});
                Assertions.assertFalse(creator.emailAlreadyExist("notexisting@me.com"));
            }
        }

        @Test
        public void existsInAccountNotInTemp() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> {});
                Assertions.assertTrue(creator.emailAlreadyExist("existing@e.com"));
            }
        }

        @Test
        public void existsInTempNotInAccount() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> {});
                Assertions.assertTrue(creator.emailAlreadyExist("existingTemp@e.com"));
            }
        }
    }

    @Nested
    public class Validate {

        private DataBaseConnectionProvider givenAConnexionProvider() throws Exception {
            Thread.sleep(500);
            return new TestingDatabaseInit().init("test_db.xml");
        }

        @Test
        public void happyFlow() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                Result message = new Result();
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> message.value = m);
                creator.confirm(givenAnAccountValidationDto());
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
        public void validationFails() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                Result message = new Result();
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> message.value = m);
                creator.confirm(givenAWrongAccountValidationDto());
                Assertions.assertEquals("", message.value);
                try (Connection c = dbcp.getConnection();
                     PreparedStatement stmt = c.prepareStatement("SELECT * FROM TEMP_ACCOUNTS WHERE LOGIN = 'existingTemp2'");
                     ResultSet resultSet = stmt.executeQuery()) {
                    Assertions.assertTrue(resultSet.next());
                }
                try (Connection c = dbcp.getConnection();
                     PreparedStatement stmt = c.prepareStatement("SELECT * FROM ACCOUNTS WHERE LOGIN = 'existingTemp2'");
                     ResultSet resultSet = stmt.executeQuery()) {
                    Assertions.assertFalse(resultSet.next());
                }
            }
        }

        @Test
        public void accountNotExisting() throws Exception {
            try(DataBaseConnectionProvider dbcp = givenAConnexionProvider()) {
                Result message = new Result();
                DatabaseAccountCreator creator = new DatabaseAccountCreator(dbcp, (m, h) -> message.value = m);
                try (Connection c = dbcp.getConnection();
                     PreparedStatement stmt = c.prepareStatement("SELECT * FROM TEMP_ACCOUNTS WHERE LOGIN = 'tempNotExisting'");
                     ResultSet resultSet = stmt.executeQuery()) {
                    Assertions.assertFalse(resultSet.next());
                }
                creator.confirm(givenANotExistingAccountValidationDto());
                try (Connection c = dbcp.getConnection();
                     PreparedStatement stmt = c.prepareStatement("SELECT * FROM ACCOUNTS WHERE LOGIN = 'tempNotExisting'");
                     ResultSet resultSet = stmt.executeQuery()) {
                    Assertions.assertFalse(resultSet.next());
                }
            }
        }

        @Test
        public void insertionFails() {
            //TODO check for rollback
        }

        @Test
       public void deleteFails() {
            //TODO check for rollback
        }

        AccountConfirmationDto givenAWrongAccountValidationDto() {
            return new AccountConfirmationDto("existingTemp", "123");
        }


        AccountConfirmationDto givenANotExistingAccountValidationDto() {
            return new AccountConfirmationDto("tempNotExisting", "1234");
        }

    }

    static AccountConfirmationDto givenAnAccountValidationDto() {
        return new AccountConfirmationDto("existingTemp", "azerty");
    }

    private class Result {

        private String value = "";


    }*/


}
