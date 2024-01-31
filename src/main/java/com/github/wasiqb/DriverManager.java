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
import static java.time.Duration.ofMillis;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;

import io.appium.java_client.Setting;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidStartScreenRecordingOptions;
import io.appium.java_client.android.AndroidStopScreenRecordingOptions;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

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
        this.driver.setSetting (Setting.IGNORE_UNIMPORTANT_VIEWS, true);

       startStreaming ();
       startRecording ();
        swipe ();
    }

    public void close () {
        stopStreaming ();
        stopRecording ();
        this.driver.quit ();
        if (this.service.isRunning ()) {
            this.service.stop ();
        }
    }

    public AndroidDriver getDriver () {
        return this.driver;
    }

    private AppiumDriverLocalService buildAppiumService () {
        final var logFile = Path.of (getProperty ("user.dir"), "logs", "appium.log")
            .toFile ();
        final var builder = new AppiumServiceBuilder ();
        return builder.withIPAddress (getProperty ("host", "127.0.0.1"))
            .usingPort (parseInt (getProperty ("port", "4723")))
            .withLogFile (logFile)
            .withArgument (BASEPATH, "/wd/hub")
            .withArgument (USE_DRIVERS, "uiautomator2")
            .withArgument (SESSION_OVERRIDE)
            .withArgument (ALLOW_INSECURE, "chromedriver_autodownload,adb_screen_streaming")
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

    private void saveRecording (final String content) {
        final var decode = Base64.getDecoder ()
            .decode (content);
        try {
            final var date = new SimpleDateFormat ("yyyyMMdd-HHmmss");
            final var timeStamp = date.format (Calendar.getInstance ()
                .getTime ());
            final var fileName = format ("{0}/videos/VID-{1}.mp4", System.getProperty ("user.dir"), timeStamp);
            FileUtils.writeByteArrayToFile (new File (fileName), decode);
        } catch (final IOException e) {
            e.printStackTrace ();
        }
    }

    private void startRecording () {
        final var option = AndroidStartScreenRecordingOptions.startScreenRecordingOptions ()
            .withTimeLimit (Duration.ofMinutes (5));
        this.driver.startRecordingScreen (option);
    }

    private void startStreaming () {
        final var args = new HashMap<String, Object> ();
        args.put ("host", "127.0.0.1");
        args.put ("port", 8093);
        args.put ("quality", 75);
        args.put ("bitRate", 20000000);
        this.driver.executeScript ("mobile: startScreenStreaming", args);
    }

    private void stopRecording () {
        final var option = AndroidStopScreenRecordingOptions.stopScreenRecordingOptions ();
        final var videoContent = this.driver.stopRecordingScreen (option);
        saveRecording (videoContent);
    }

    private void stopStreaming () {
        this.driver.executeScript ("mobile: stopScreenStreaming");
    }

    private void swipe () {
        final var finger = new PointerInput (PointerInput.Kind.TOUCH, "Finger 1");
        final var sequence = new Sequence (finger, 0);
        final var size = this.driver.manage ()
            .window ()
            .getSize ();
        final var start = new Point (size.getWidth () / 2, size.getHeight () / 2);
        final var end = new Point (start.getX (), start.getY () - (start.getY () / 2));
        sequence.addAction (
            finger.createPointerMove (Duration.ZERO, PointerInput.Origin.viewport (), start.getX (), start.getY ()));
        sequence.addAction (finger.createPointerDown (PointerInput.MouseButton.LEFT.asArg ()));
        sequence.addAction (
            finger.createPointerMove (ofMillis (600), PointerInput.Origin.viewport (), end.getX (), end.getY ()));
        sequence.addAction (finger.createPointerUp (PointerInput.MouseButton.LEFT.asArg ()));
        this.driver.perform (singletonList (sequence));
    }
}
