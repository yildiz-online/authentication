/*
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 * Copyright (c) 2017 Grégory Van den Borre
 *
 * More infos available: https://www.yildiz-games.be
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT  HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE  SOFTWARE.
 */

package be.yildiz.authentication.configuration;

import be.yildiz.common.exeption.ResourceMissingException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * @author Grégory Van den Borre
 */
@RunWith(Enclosed.class)
public class ConfigurationTest {

    public static class FromArgs {

        @Test
        public void happyFlow() {
            Configuration.fromAppArgs(new String[] {getFile("test-happyflow.properties").getAbsolutePath()});
        }

        @Test(expected = IllegalArgumentException.class)
        public void fromIncompleteFile() {
            Configuration.fromAppArgs(new String[] {getFile("test-incomplete.properties").getAbsolutePath()});
        }

        @Test(expected = IllegalArgumentException.class)
        public void fromNull() {
            Configuration.fromAppArgs(null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void fromEmptyArgs() {
            Configuration.fromAppArgs(new String[] {});
        }

        @Test(expected = ResourceMissingException.class)
        public void notExistingFile() {
            Configuration.fromAppArgs(new String[] {"nowhere"});
        }
    }

    public static class Getter  {

        Configuration c = Configuration.fromAppArgs(new String[] {getFile("test-happyflow.properties").getAbsolutePath()});

        @Test
        public void getDatabaseUser() {
            Assert.assertEquals("user", c.getDbUser());
        }

        @Test
        public void getDatabasePassword() {
            Assert.assertEquals("pwd", c.getDbPassword());
        }

        @Test
        public void getDatabaseName() {
            Assert.assertEquals("name", c.getDbName());
        }

        @Test
        public void getDatabaseHost() {
            Assert.assertEquals("host", c.getDbHost());
        }

        @Test
        public void getDatabasePort() {
            Assert.assertEquals(123, c.getDbPort());
        }

        @Test
        public void getDatabaseSystem() {
            Assert.assertEquals("sys", c.getSystem());
        }

        @Test
        public void getNetworkPort() {
            Assert.assertEquals(456, c.getAuthenticationPort());
        }

        @Test
        public void getNetworkHost() {
            Assert.assertEquals("nhost", c.getAuthenticationHost());
        }

    }

    private static File getFile(String name) {
        return new File(Configuration.class.getClassLoader().getResource(name).getFile()).getAbsoluteFile();
    }
}
