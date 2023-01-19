package com.github.wasiqb.pages;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HybridPage {
    private final AndroidDriver driver;
    private final WebDriverWait wait;

    public HybridPage (final AndroidDriver driver, final WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public WebElement browserTab () {
        return this.wait.until (visibilityOfElementLocated (AppiumBy.accessibilityId ("Browser")));
    }

    public void navigateTo (final String url) {
        browserTab ().click ();
        url ().sendKeys (url);
        this.driver.hideKeyboard ();
        find ().click ();
    }

    public String webTitle () {
        return this.wait.until (visibilityOfElementLocated (By.tagName ("h1")))
            .getText ();
    }

    private WebElement find () {
        return this.wait.until (visibilityOfElementLocated (AppiumBy.id ("find")));
    }

    private WebElement url () {
        return this.wait.until (visibilityOfElementLocated (AppiumBy.id ("url")));
    }
}
