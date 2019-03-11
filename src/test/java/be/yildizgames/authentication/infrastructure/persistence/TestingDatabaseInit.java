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

import be.yildizgames.module.database.DataBaseConnectionProvider;
import be.yildizgames.module.database.DatabaseConnectionProviderFactory;
import be.yildizgames.module.database.DbProperties;
import be.yildizgames.module.database.LiquibaseDatabaseUpdater;

import java.sql.SQLException;

/**
 * @author Grégory Van den Borre
 */
public class TestingDatabaseInit {

    public DataBaseConnectionProvider init(final String changeLogFile) throws SQLException {
        DatabaseConnectionProviderFactory.getInstance().addSystem("h2", new DummySystem());
        DataBaseConnectionProvider dbcp =
                DatabaseConnectionProviderFactory.getInstance().create(new TestingDbProperties());
        LiquibaseDatabaseUpdater.fromConfigurationPath(changeLogFile).update(dbcp);
        return dbcp;
    }

    public DataBaseConnectionProvider init(final String changeLogFile, final String databaseName) throws SQLException {
        DatabaseConnectionProviderFactory.getInstance().addSystem("h2", new DummySystem());
        DataBaseConnectionProvider dbcp =
                DatabaseConnectionProviderFactory.getInstance().create(new TestingDbProperties(databaseName));
        LiquibaseDatabaseUpdater.fromConfigurationPath(changeLogFile).update(dbcp);
        return dbcp;
    }


    /**
     * @author Grégory Van den Borre
     */
    static class TestingDbProperties implements DbProperties {

        private final String name;

        public TestingDbProperties() {
            this("YILDIZDATABASE");
        }

         public TestingDbProperties(String databaseName) {
             this.name = databaseName;
         }

        @Override
        public String getDbUser() {
            return name;
        }

        @Override
        public int getDbPort() {
            return 0;
        }

        @Override
        public String getDbPassword() {
            return "";
        }

        @Override
        public String getDbHost() {
            return "";
        }

        @Override
        public String getDbName() {
            return name;
        }

        @Override
        public String getSystem() {
            return "h2";
        }

        @Override
        public String getDbRootUser() {
            return getDbUser();
        }

        @Override
        public String getDbRootPassword() {
            return getDbPassword();
        }
    }
}
