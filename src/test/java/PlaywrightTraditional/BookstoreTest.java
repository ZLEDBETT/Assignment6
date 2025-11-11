package PlaywrightTraditional;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class BookstoreTest {
    Playwright playwright;
    Browser browser;
    BrowserContext context;
    Page page;

    @BeforeEach
    void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)
        );
        // Video recording
        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setRecordVideoDir(Paths.get("videos/"))
                        .setRecordVideoSize(1280, 720)
        );
        page = context.newPage();
        context.clearCookies();
    }

    @AfterEach
    void teardown() {
        context.close();
        browser.close();
        playwright.close();
    }


    private void selectFacet(String facetName, String optionText) {

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                        .setName(Pattern.compile("^" + facetName + "$", Pattern.CASE_INSENSITIVE)))
                .click();
        page.locator("label")
                .filter(new Locator.FilterOptions().setHasText(optionText))
                .first()
                .click();
    }

    @Test
    void testFullBookstoreCheckoutFlow() {
        //Load site
        page.navigate("https://depaul.bncollege.com/");
        page.waitForLoadState(LoadState.NETWORKIDLE);

        //Search earbuds
        assertThat(page.locator("input#bned_site_search")).isVisible();
        page.locator("input#bned_site_search").click();
        page.locator("input#bned_site_search").fill("earbuds");
        page.locator("input#bned_site_search").press("Enter");

        //Apply filters
        selectFacet("Brand", "JBL");
        selectFacet("Color", "Black");
        selectFacet("Price", "Over $50");

        //Select product
        page.getByRole(AriaRole.LINK,
                        new Page.GetByRoleOptions().setName(Pattern.compile("JBL Quantum True Wireless")))
                .click();

        // Product page waits
        assertThat(
                page.getByRole(
                        AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName(Pattern.compile("^JBL Quantum True Wireless"))
                ).first()
        ).isVisible();

        //Add to cart
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add to cart")).click();

        //Open cart
        page.getByRole(AriaRole.LINK,
                        new Page.GetByRoleOptions().setName(Pattern.compile("Cart.*1 item", Pattern.CASE_INSENSITIVE)))
                .click();

        assertThat(
                page.getByRole(
                        AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName(Pattern.compile("Your Shopping Cart", Pattern.CASE_INSENSITIVE))
                ).first()
        ).isVisible();

        //Promo code
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Enter Promo Code"))
                .fill("TEST");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Enter Promo Code"))
                .press("Enter");

        //Proceed checkout
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Proceed To Checkout"))
                .first().click();

        //Wait until the checkout login page has loaded
        page.waitForURL("**/checkout*");
        page.evaluate("window.scrollBy(0, 500)");
        //Proceed as guest
        Locator proceedAsGuest = page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Proceed As Guest")
        );
        assertThat(proceedAsGuest).isVisible();
        proceedAsGuest.click();

// Wait for page navigation to the new checkout page
        page.waitForURL("**/checkout/multi/**", new Page.WaitForURLOptions().setTimeout(20000));
        page.waitForLoadState(LoadState.NETWORKIDLE);

        //Contact info
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("First Name (required)"))
                .fill("Zoe");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Last Name (required)"))
                .fill("Ledbetter");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Email address (required)"))
                .fill("zoeledbetter@yahoo.com");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Phone Number (required)"))
                .fill("1234567890");

        //Continue
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue"))
                .click();

        //Pickup info
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.getByText("I'll pick them up").click();

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue"))
                .click();

        //Payment page
        page.waitForURL(Pattern.compile(".*/payment-method.*"));

        Locator orderSummaryHeader = page.getByRole(
                AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Order Summary")
        ).first();
        assertThat(orderSummaryHeader).isVisible();

        //Back to cart -> remove product
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Back to cart")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(
                Pattern.compile("Remove product JBL", Pattern.CASE_INSENSITIVE)
        )).click();
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Your cart is empty")))
                .isVisible();
    }
}
