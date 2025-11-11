package PlaywrightTraditional;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;


import java.nio.file.Paths;

public class BaseTest {

    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void launchBrowser() {
        // Start Playwright once for entire test run
        playwright = Playwright.create();

        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)   // set to true on GitHub Actions
        );
    }

    @BeforeEach
    void setUpContext() {
        // Create fresh browser context for each test, plus video recording
        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setRecordVideoDir(Paths.get("videos/"))
                        .setRecordVideoSize(1280, 720)
        );

        page = context.newPage();

        // Clear browser state before each test
        context.clearCookies();
        context.clearPermissions();
        page.navigate("https://depaul.bncollege.com/");
        page.evaluate("localStorage.clear()");

    }

    @AfterEach
    void closeContext() {
        // Saves video + frees resources
        context.close();
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }
}
