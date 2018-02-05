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

package be.yildiz.authentication.configuration;

import be.yildizgames.common.exception.technical.InitializationException;
import be.yildizgames.common.exception.technical.ResourceMissingException;
import be.yildizgames.common.util.PropertiesException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Grégory Van den Borre
 */
class ConfigurationTest {

    @Nested
    class FromArgs {

        @Test
        void happyFlow() {
            Configuration.fromAppArgs(new String[] {getFile("test-happyflow.properties").getAbsolutePath()});
        }

        @Test
        void fromIncompleteFile() {
            assertThrows(PropertiesException.class, () -> Configuration.fromAppArgs(new String[] {getFile("test-incomplete.properties").getAbsolutePath()}));
        }

        @Test
        void fromNull() {
            assertThrows(InitializationException.class, () -> Configuration.fromAppArgs(null));
        }

        @Test
        void fromEmptyArgs() {
            assertThrows(InitializationException.class, () -> Configuration.fromAppArgs(new String[] {}));
        }

        @Test
        void notExistingFile() {
            assertThrows(ResourceMissingException.class, () -> Configuration.fromAppArgs(new String[] {"nowhere"}));
        }
    }

    @Nested
    class Getter  {

        Configuration c = Configuration.fromAppArgs(new String[] {getFile("test-happyflow.properties").getAbsolutePath()});

        @Test
        void getDatabaseUser() {
            assertEquals("user", c.getDbUser());
        }

        @Test
        void getDatabasePassword() {
            assertEquals("pwd", c.getDbPassword());
        }

        @Test
        void getDatabaseName() {
            assertEquals("name", c.getDbName());
        }

        @Test
        void getDatabaseHost() {
            assertEquals("host", c.getDbHost());
        }

        @Test
        void getDatabasePort() {
            assertEquals(123, c.getDbPort());
        }

        @Test
        void getDatabaseSystem() {
            assertEquals("sys", c.getSystem());
        }


    }

    private static File getFile(String name) {
        return new File(Configuration.class.getClassLoader().getResource(name).getFile()).getAbsoluteFile();
    }
}
