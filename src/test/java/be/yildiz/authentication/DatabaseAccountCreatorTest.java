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

import be.yildiz.module.database.DataBaseConnectionProvider;
import be.yildiz.module.database.TestingDatabaseInit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * @author Grégory Van den Borre
 */
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
    }
}
