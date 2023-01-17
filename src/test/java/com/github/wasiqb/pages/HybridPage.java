package com.github.wasiqb.pages;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HybridPage {
    private final AndroidDriver driver;
    private final WebDriverWait wait;

    public HybridPage (final AndroidDriver driver, final WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void navigateTo (final String url) {
        this.driver.findElement (AppiumBy.accessibilityId ("Browser"))
            .click ();
        this.wait.until (visibilityOfElementLocated (AppiumBy.id ("url")))
            .sendKeys (url);
        this.driver.hideKeyboard ();
        this.driver.findElement (AppiumBy.id ("find"))
            .click ();
    }
}
