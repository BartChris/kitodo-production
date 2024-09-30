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

package org.kitodo.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.CalendarPage;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.test.utils.ProcessTestUtils;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CalendarST extends BaseTestSelenium {

    private static ProcessesPage processesPage;
    private static CalendarPage calendarPage;
    private static int newspaperTestProcessId = -1;
    private static final String NEWSPAPER_TEST_METADATA_FILE = "testmetaNewspaper.xml";
    private static final String NEWSPAPER_TEST_PROCESS_TITLE = "NewspaperOverallProcess";

    @BeforeAll
    public static void setup() throws Exception {
        processesPage = Pages.getProcessesPage();
        calendarPage = Pages.getCalendarPage();
        int rulesetId = MockDatabase.insertRuleset("Newspaper", "newspaper.xml", 1);
        newspaperTestProcessId = MockDatabase.insertTestProcess(NEWSPAPER_TEST_PROCESS_TITLE, 1, 1, rulesetId);
        ProcessTestUtils.copyTestMetadataFile(newspaperTestProcessId, NEWSPAPER_TEST_METADATA_FILE);
    }

    @BeforeEach
    public void login() throws Exception {
        User calendarUser = ServiceManager.getUserService().getByLogin("kowal");
        Pages.getLoginPage().goTo().performLogin(calendarUser);
    }

    @AfterEach
    public void logout() throws Exception {
        calendarPage.closePage();
        Pages.getTopNavigation().logout();
    }

    @AfterAll
    public static void cleanup() throws CustomResponseException, DAOException, DataException, IOException {
        ProcessTestUtils.removeTestProcess(newspaperTestProcessId);
    }


    @Test
    public void createProcessFromCalendar() throws Exception {
        // Navigate to the processes page and open the calendar
        processesPage.goTo();
        processesPage.goToCalendar(newspaperTestProcessId);

        // Add blocks and issues
        calendarPage.addBlock();
        calendarPage.addIssue("Morning issue");
        calendarPage.addIssue("Evening issue");

        // Assert the number of issues is correct
        assertEquals(4, calendarPage.countIssues(), "Number of issues in the calendar does not match");

        // Add metadata to issues
        calendarPage.addMetadataToThis();
        calendarPage.addMetadataToAll();

        // Fetch "Morning issue" metadata with WebDriverWait
        List<String> morningIssueMetadata = fetchMetadataWithRetry("Morning issue");

        // Fetch "Evening issue" metadata with WebDriverWait
        List<String> eveningIssueMetadata = fetchMetadataWithRetry("Evening issue");

        // Verify that the metadata is correct
        assertEquals(Arrays.asList("Signatur", "Process title"), morningIssueMetadata, "Metadata for morning issue is incorrect");
        assertEquals(List.of("Signatur"), eveningIssueMetadata, "Metadata for evening issue is incorrect");
    }

    // Method to fetch metadata with retry logic to handle stale element exceptions
    private List<String> fetchMetadataWithRetry(String issueName) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 15);
                List<WebElement> metadataElements = wait.until(ExpectedConditions.visibilityOfAllElements(
                        calendarPage.getMetadataElements(issueName)
                ));
                return calendarPage.extractMetadataFromElements(metadataElements);
            } catch (StaleElementReferenceException e) {
                attempts++;
                System.out.println("Attempt " + attempts + ": Stale element detected. Retrying...");
            } catch (TimeoutException e) {
                // Handle timeout here if needed
                System.out.println("Attempt " + attempts + ": Timeout waiting for elements. Retrying...");
                attempts++;
            }
        }
        throw new StaleElementReferenceException("Failed to fetch metadata after 3 attempts due to stale element issues");
    }
}
