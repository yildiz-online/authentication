/*
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 *  Copyright (c) 2019 Grégory Van den Borre
 *
 *  More infos available: https://engine.yildiz-games.be
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

package be.yildizgames.authentication.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Grégory Van den Borre
 */
public class ConfigurationTest {

    @Nested
    public class fromProperties {

        @Test
        public void happyFlow() {
            Configuration c = new Configuration(DefaultConfigProperties.create());
            Assertions.assertNotNull(c);
        }

        @Test
        public void fromNull() {
            assertThrows(NullPointerException.class, () -> new Configuration(null));
        }
    }

    @Nested
    public class Getter  {

        final Configuration c = new Configuration(DefaultConfigProperties.create());

        @Test
        public void getDatabaseUser() {
            assertEquals("sa", c.getDbUser());
        }

        @Test
        public void getDatabasePassword() {
            assertEquals("sa", c.getDbPassword());
        }

        @Test
        public void getDatabaseName() {
            assertEquals("database", c.getDbName());
        }

        @Test
        public void getDatabaseHost() {
            assertEquals("localhost", c.getDbHost());
        }

        @Test
        public void getDatabasePort() {
            assertEquals(9000, c.getDbPort());
        }

        @Test
        public void getDatabaseSystem() {
            assertEquals("derby-file", c.getSystem());
        }
    }
}
