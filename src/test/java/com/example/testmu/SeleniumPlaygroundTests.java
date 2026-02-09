package com.example.testmu;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class SeleniumPlaygroundTests {

    static Stream<Arguments> browserMatrix() {
        return Stream.of(
                Arguments.of("chrome", "128.0", "Windows 10"),
                Arguments.of("MicrosoftEdge", "127.0", "macOS Ventura"),
                Arguments.of("firefox", "130.0", "Windows 11"),
                Arguments.of("internet explorer", "11.0", "Windows 10")
        );
    }

    private WebDriverWait wait(RemoteWebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @DisplayName("1) Radio Buttons Demo — validate Female selection message")
    @Timeout(value = 30)
    @ParameterizedTest(name = "{index} => {0} {1} on {2}")
    @MethodSource("browserMatrix")
    void radioButtonsDemo(String browser, String version, String platform) throws MalformedURLException, InterruptedException {
        String testName = "Radio Buttons Demo — " + browser + " " + version + " on " + platform;
        RemoteWebDriver driver = WebDriverHelper.createRemoteDriver(browser, version, platform, testName);
        try {
            String url = "https://www.testmuai.com/selenium-playground/";
            driver.get(url);
            WebDriverHelper.waitForDomReady(driver, Duration.ofSeconds(20));
            WebDriverHelper.takeScreenshot(driver, "01-home");

            // Click "Radio Buttons Demo"
            wait(driver).until(ExpectedConditions.elementToBeClickable(By.linkText("Radio Buttons Demo"))).click();
            WebDriverHelper.takeScreenshot(driver, "02-radio-page");

            // Select Female using CSS (locator 1) - Fast JavaScript execution
            ((JavascriptExecutor) driver).executeScript(
                "var female = document.querySelector(\"input[name='gender'][value='Female']\");" +
                "if(female) { female.checked = true; female.click(); }"
            );

            // Click "Get value" using XPath (locator 2) - Fast JavaScript execution  
            ((JavascriptExecutor) driver).executeScript(
                "var btn = document.evaluate(\"//button[normalize-space()='Get value']\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;" +
                "if(btn) btn.click();"
            );

            // Read message using a specific locator (locator 3) — the paragraph element with the validation message
            WebElement message = wait(driver).until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//p[starts-with(normalize-space(), 'Radio button') and contains(., 'checked')]")
            ));

            String actual = message.getText().trim();
            String expected = "Radio button 'Female' is checked";
            assertEquals(expected, actual, "Validation message mismatch");

            System.out.println("Session ID: " + driver.getSessionId());
            WebDriverHelper.dumpConsoleLogs(driver);
        } finally {
            WebDriverHelper.takeScreenshot(driver, "99-end");
            driver.quit(); // single command to close all windows
        }
    }

    @DisplayName("2) Window Popup Modal — validate new window and close all")
    @Timeout(value = 30)
    @ParameterizedTest(name = "{index} => {0} {1} on {2}")
    @MethodSource("browserMatrix")
    void windowPopupModal(String browser, String version, String platform) throws MalformedURLException {
        String testName = "Window Popup Modal — " + browser + " " + version + " on " + platform;
        RemoteWebDriver driver = WebDriverHelper.createRemoteDriver(browser, version, platform, testName);
        try {
            String url = "https://www.testmuai.com/selenium-playground/";
            driver.get(url);
            WebDriverHelper.waitForDomReady(driver, Duration.ofSeconds(20));
            WebDriverHelper.takeScreenshot(driver, "01-home");

            // Click "Window Popup Modal"
            wait(driver).until(ExpectedConditions.elementToBeClickable(By.linkText("Window Popup Modal"))).click();

            // Click "Follow On Twitter" (by its visible text) - use JavaScript to avoid stale element
            wait(driver).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[normalize-space()='Follow On Twitter' or normalize-space()='Follow on Twitter' or contains(., 'Twitter')]")));
            ((JavascriptExecutor) driver).executeScript(
                "var link = document.evaluate(\"//a[normalize-space()='Follow On Twitter' or normalize-space()='Follow on Twitter' or contains(., 'Twitter')]\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;" +
                "if(link) link.click();"
            );

            // Validate a new window is opened using window handles
            String main = driver.getWindowHandle();
            wait(driver).until(d -> d.getWindowHandles().size() > 1);
            Set<String> handles = driver.getWindowHandles();
            assertTrue(handles.size() > 1, "A new window should have opened");

            // Switch to the new window and perform a lightweight check
            for (String h : handles) {
                if (!h.equals(main)) {
                    driver.switchTo().window(h);
                    break;
                }
            }
            WebDriverHelper.takeScreenshot(driver, "02-new-window");
            System.out.println("Session ID: " + driver.getSessionId());
            WebDriverHelper.dumpConsoleLogs(driver);
        } finally {
            driver.quit(); // single command to close all windows
        }
    }
}
