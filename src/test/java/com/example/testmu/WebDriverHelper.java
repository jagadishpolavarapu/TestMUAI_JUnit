package com.example.testmu;

import org.openqa.selenium.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;

public class WebDriverHelper {

    public static RemoteWebDriver createRemoteDriver(String browserName, String browserVersion, String platformName, String testName) throws MalformedURLException {
        String username = System.getenv("TESTMU_USERNAME");
        String accessKey = System.getenv("TESTMU_ACCESS_KEY");
        String gridUrl = System.getenv("TESTMU_GRID_URL");

        // If no grid URL is set, create a local driver instead
        if (gridUrl == null || gridUrl.isBlank()) {
            System.out.println("WARNING: TESTMU_GRID_URL not set - using LOCAL driver instead");
            return createLocalDriver(browserName, testName);
        }

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setBrowserName(browserName);
        if (browserVersion != null && !browserVersion.isBlank()) {
            caps.setVersion(browserVersion);
        }
        if (platformName != null && !platformName.isBlank()) {
            caps.setCapability("platformName", platformName);
        }

        // Console logs
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        caps.setCapability("goog:loggingPrefs", logPrefs);

        // LambdaTest options (TestMU AI uses LambdaTest format)
        Map<String, Object> ltOptions = new HashMap<>();
        ltOptions.put("username", username);
        ltOptions.put("accessKey", accessKey);
        ltOptions.put("build", System.getenv().getOrDefault("BUILD_NAME", "JUnit Final Submission - Feb 9 2026"));
        ltOptions.put("name", testName);
        ltOptions.put("platformName", platformName);
        
        // Enable all required capabilities - video, network logs, console logs, screenshots
        ltOptions.put("video", true);           // Video recording enabled
        ltOptions.put("network", true);         // Network logs enabled
        ltOptions.put("console", true);         // Console logs enabled  
        ltOptions.put("visual", true);          // Visual logs/screenshots enabled
        ltOptions.put("terminal", true);        // Command logs
        ltOptions.put("w3c", true);
        ltOptions.put("plugin", "java-junit");
        
        caps.setCapability("LT:Options", ltOptions);

        URL url = new URL(gridUrl);
        RemoteWebDriver driver = new RemoteWebDriver(url, caps);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0)); // we use explicit waits
        driver.manage().window().setSize(new Dimension(1400, 900));
        return driver;
    }

    public static void waitForDomReady(WebDriver driver, Duration timeout) {
        new org.openqa.selenium.support.ui.WebDriverWait(driver, timeout)
                .until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    public static void takeScreenshot(WebDriver driver, String name) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path dir = Path.of("target", "screenshots");
            Files.createDirectories(dir);
            String fileName = name + "_" + UUID.randomUUID() + ".png";
            FileUtils.copyFile(src, dir.resolve(fileName).toFile());
        } catch (IOException e) {
            System.err.println("Failed to save screenshot: " + e.getMessage());
        }
    }

    public static void dumpConsoleLogs(RemoteWebDriver driver) {
        try {
            LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
            logs.forEach(entry -> System.out.printf("[BROWSER] %s %s%n", entry.getLevel(), entry.getMessage()));
        } catch (Exception ignored) {
        }
    }

    private static RemoteWebDriver createLocalDriver(String browserName, String testName) {
        System.out.println("Creating LOCAL " + browserName + " driver for: " + testName);
        
        WebDriver driver;
        if (browserName.equalsIgnoreCase("chrome")) {
            org.openqa.selenium.chrome.ChromeOptions options = new org.openqa.selenium.chrome.ChromeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--disable-blink-features=AutomationControlled");
            driver = new org.openqa.selenium.chrome.ChromeDriver(options);
        } else if (browserName.equalsIgnoreCase("firefox")) {
            org.openqa.selenium.firefox.FirefoxOptions options = new org.openqa.selenium.firefox.FirefoxOptions();
            driver = new org.openqa.selenium.firefox.FirefoxDriver(options);
        } else if (browserName.equalsIgnoreCase("MicrosoftEdge") || browserName.equalsIgnoreCase("edge")) {
            org.openqa.selenium.edge.EdgeOptions options = new org.openqa.selenium.edge.EdgeOptions();
            driver = new org.openqa.selenium.edge.EdgeDriver(options);
        } else {
            // Default to Chrome
            System.out.println("WARNING: Browser '" + browserName + "' not supported locally, using Chrome instead");
            org.openqa.selenium.chrome.ChromeOptions options = new org.openqa.selenium.chrome.ChromeOptions();
            options.addArguments("--start-maximized");
            driver = new org.openqa.selenium.chrome.ChromeDriver(options);
        }
        
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        driver.manage().window().setSize(new Dimension(1400, 900));
        
        // Cast to RemoteWebDriver for consistency
        return (RemoteWebDriver) driver;
    }
}
