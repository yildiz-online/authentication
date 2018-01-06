/*
 *  This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 *  Copyright (c) 2017 Grégory Van den Borre
 *
 *  More infos available: https://www.yildiz-games.be
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE  SOFTWARE.
 */

package be.yildiz.authentication.main;

import java.util.Properties;

public class GitProperties {

    private static final String DEFAULT = "unknown";

    private final String branch;

    private final String commiter;

    private final String version;

    private final String buildTime;

    private final String commitId;

    private final String commitTime;

    private final String commitMessage;

    public GitProperties(Properties p) {
        super();
        this.branch = p.getProperty("git.branch", DEFAULT);
        this.commiter = p.getProperty("git.commit.user.name", DEFAULT);
        this.version = p.getProperty("git.build.version", DEFAULT);
        this.buildTime = p.getProperty("git.build.time", DEFAULT);
        this.commitId = p.getProperty("git.commit.id", DEFAULT);
        this.commitTime = p.getProperty("git.commit.time", DEFAULT);
        this.commitMessage = p.getProperty("git.commit.message.full", DEFAULT);
    }

    public String getBranch() {
        return branch;
    }

    public String getCommiter() {
        return commiter;
    }

    public String getVersion() {
        return version;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getCommitTime() {
        return commitTime;
    }

    public String getCommitMessage() {
        return commitMessage;
    }
}
