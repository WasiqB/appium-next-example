package com.github.wasiqb;

import static com.google.common.truth.Truth.assertThat;

import java.time.Duration;

import com.github.wasiqb.pages.HybridPage;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class AndroidTest {
    private AndroidDriver driver;
    private DriverManager driverManager;

    @Parameters ("isCloud")
    @BeforeTest (alwaysRun = true)
    public void setupTest (final boolean isCloud) {
        this.driverManager = DriverManager.createDriver (isCloud);
        this.driver = this.driverManager.getDriver ();
    }

    @AfterTest (alwaysRun = true)
    public void teardownTest () {
        this.driverManager.close ();
    }

    @Test
    public void testHybridScreen () {
        final var wait = new WebDriverWait (this.driver, Duration.ofSeconds (5));

        final var hybridPage = new HybridPage (this.driver, wait);
        hybridPage.navigateTo ("https://www.lambdatest.com");

        wait.until (d -> this.driver.getContextHandles ()
            .size () > 1);

        this.driver.context ("WEBVIEW_com.lambdatest.proverbial");

        assertThat (hybridPage.webTitle ()).isEqualTo ("Next-Generation Mobile Apps and Cross Browser Testing Cloud");

        this.driver.context ("NATIVE_APP");

        assertThat (hybridPage.browserTab ()
            .isEnabled ()).isTrue ();
    }
}
