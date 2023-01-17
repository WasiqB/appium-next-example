package com.github.wasiqb;

import static io.appium.java_client.service.local.flags.GeneralServerFlag.ALLOW_INSECURE;
import static io.appium.java_client.service.local.flags.GeneralServerFlag.BASEPATH;
import static io.appium.java_client.service.local.flags.GeneralServerFlag.SESSION_OVERRIDE;
import static io.appium.java_client.service.local.flags.GeneralServerFlag.USE_DRIVERS;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import org.openqa.selenium.Capabilities;

public class DriverManager {
    private static final String DEVICE_NAME_KEY    = "deviceName";
    private static final String DEVICE_VERSION_KEY = "deviceVersion";

    public static DriverManager createDriver (final boolean isCloud) {
        return new DriverManager (isCloud);
    }

    private final AndroidDriver            driver;
    private       AppiumDriverLocalService service;

    private DriverManager (final boolean isCloud) {
        final Capabilities capabilities;
        final URL serverUrl;
        if (isCloud) {
            serverUrl = getCloudUrl ();
            capabilities = buildCloudCapabilities ();
        } else {
            capabilities = buildCapabilities ();
            this.service = buildAppiumService ();
            this.service.start ();
            serverUrl = this.service.getUrl ();
        }
        this.driver = new AndroidDriver (serverUrl, capabilities);
        this.driver.setSetting ("ignoreUnimportantViews", true);
    }

    public void close () {
        this.driver.quit ();
        if (this.service != null) {
            this.service.stop ();
        }
    }

    public AndroidDriver getDriver () {
        return this.driver;
    }

    private AppiumDriverLocalService buildAppiumService () {
        final var appiumPath = Path.of (getenv ("HOME"),
                ".nvm/versions/node/v16.19.0/lib/node_modules/appium/build/lib/main.js")
            .toFile ();
        final var logFile = Path.of (getProperty ("user.dir"), "logs", "appium.log")
            .toFile ();
        final var builder = new AppiumServiceBuilder ();
        return builder.withIPAddress (getProperty ("host", "127.0.0.1"))
            .usingPort (parseInt (getProperty ("port", "4723")))
            .withLogFile (logFile)
            .withAppiumJS (appiumPath)
            .withArgument (BASEPATH, "/wd/hub")
            .withArgument (USE_DRIVERS, "uiautomator2")
            .withArgument (SESSION_OVERRIDE)
            .withArgument (ALLOW_INSECURE, "chromedriver_autodownload")
            .build ();
    }

    private Capabilities buildCapabilities () {
        final var deviceName = getProperty (DEVICE_NAME_KEY, "Pixel_6_Pro");
        final var deviceVersion = getProperty (DEVICE_VERSION_KEY, "11");
        final var options = new UiAutomator2Options ();
        options.setPlatformName ("Android")
            .setPlatformVersion (deviceVersion)
            .setDeviceName (deviceName)
            .setAvd (deviceName)
            .setApp (Path.of (getProperty ("user.dir"), "src/test/resources/proverbial.apk")
                .toString ())
            .setAutoGrantPermissions (true)
            .setIsHeadless (parseBoolean (getProperty ("headless", "false")));
        return options;
    }

    private Capabilities buildCloudCapabilities () {
        final var deviceName = getProperty (DEVICE_NAME_KEY, "Pixel_6_Pro");
        final var deviceVersion = getProperty (DEVICE_VERSION_KEY, "11");
        final var options = new UiAutomator2Options ();
        final var ltOptions = new HashMap<> ();
        ltOptions.put ("w3c", true);
        ltOptions.put ("platformName", "Android");
        ltOptions.put (DEVICE_NAME_KEY, deviceName);
        ltOptions.put ("platformVersion", deviceVersion);
        ltOptions.put ("app", requireNonNull (getenv ("LT_APP_ANDROID"), "Cloud App URL is required"));
        ltOptions.put ("devicelog", true);
        ltOptions.put ("visual", true);
        ltOptions.put ("network", true);
        ltOptions.put ("video", true);
        ltOptions.put ("build", "Appium sample Build");
        ltOptions.put ("name", "Android Sample");
        ltOptions.put ("project", "Appium Sample Project");
        ltOptions.put ("autoGrantPermissions", true);
        ltOptions.put ("isRealMobile", true);
        options.setCapability ("lt:options", ltOptions);
        return options;
    }

    private URL getCloudUrl () {
        final var cloudUrl = "https://{0}:{1}@mobile-hub.lambdatest.com/wd/hub";
        final var userName = requireNonNull (getenv ("LT_USERNAME"), "Cloud user name is required");
        final var key = requireNonNull (getenv ("LT_ACCESS_KEY"), "Cloud access key is required");
        final var path = format (cloudUrl, userName, key);
        try {
            return new URL (path);
        } catch (final MalformedURLException e) {
            throw new UnsupportedOperationException (format ("URL malformed: {0}", path));
        }
    }
}
