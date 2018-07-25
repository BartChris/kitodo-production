/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.selenium.testframework.helper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.kitodo.selenium.testframework.Browser;
import org.openqa.selenium.WebDriverException;

public class TestWatcherImpl extends TestWatcher {

    private static final Logger logger = LogManager.getLogger(TestWatcherImpl.class);

    private static final String TRAVIS_BUILD_NUMBER = "TRAVIS_BUILD_NUMBER";
    private static final String TRAVIS_BRANCH = "TRAVIS_BRANCH";
    private static final String TRAVIS_REPO_SLUG = "TRAVIS_REPO_SLUG";
    private static final String TRAVIS_BUILD_ID = "TRAVIS_BUILD_ID";
    private static final String MAIL_USER = "MAIL_USER";
    private static final String MAIL_PASSWORD = "MAIL_PASSWORD";
    private static final String MAIL_RECIPIENT = "MAIL_RECIPIENT";

    @Override
    protected void failed(Throwable ex, Description description) {
        if (Browser.isOnTravis() && (ex instanceof WebDriverException)) {
            try {
                File screenshot = Browser.captureScreenShot();
                Map<String, String> travisProperties = getTravisProperties();

                String emailSubject = String.format("%s - #%s: Test Failure: %s: %s",
                    travisProperties.get(TRAVIS_BRANCH), travisProperties.get(TRAVIS_BUILD_NUMBER),
                    description.getClassName(), description.getMethodName());

                String emailMessage = String.format(
                    "Selenium Test failed on build #%s: https://travis-ci.org/%s/builds/%s",
                    travisProperties.get(TRAVIS_BUILD_NUMBER), travisProperties.get(TRAVIS_REPO_SLUG),
                    travisProperties.get(TRAVIS_BUILD_ID));

                String user = travisProperties.get(MAIL_USER);
                String password = travisProperties.get(MAIL_PASSWORD);
                String recipient = travisProperties.get(MAIL_RECIPIENT);

                MailSender.sendEmail(user, password, emailSubject, emailMessage, screenshot, recipient);
            } catch (Exception mailException) {
                logger.error("Unable to send screenshot", mailException);
            }
        }
        super.failed(ex, description);
    }

    private Map<String, String> getTravisProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put(TRAVIS_BRANCH, System.getenv().get(TRAVIS_BRANCH));
        properties.put(TRAVIS_BUILD_ID, System.getenv().get(TRAVIS_BUILD_ID));
        properties.put(TRAVIS_BUILD_NUMBER, System.getenv().get(TRAVIS_BUILD_NUMBER));
        properties.put(TRAVIS_REPO_SLUG, System.getenv().get(TRAVIS_REPO_SLUG));
        properties.put(MAIL_USER, System.getenv().get(MAIL_USER));
        properties.put(MAIL_PASSWORD, System.getenv().get(MAIL_PASSWORD));
        properties.put(MAIL_RECIPIENT, System.getenv().get(MAIL_RECIPIENT));
        return properties;
    }
}
