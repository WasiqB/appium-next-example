## Environment variables

- `LT_USERNAME`: LT User Name
- `LT_ACCESS_KEY`: LT access key
- `LT_APP_ANDROID`: LT app url

## System Properties

- `host`: Default (`127.0.0.1`): Update the host name to use for local run
- `port`: Default (`4723`): Update the port number to use for local run
- `deviceName`: Default (`Pixel_6_Pro`): Update the Emulator name for local run or Cloud device name when running on
  cloud
- `deviceVersion`: Default (`11`): Update the Emulator Android version for local run or Cloud device OS version
- `headless`: Default (`false`): Update to `true` to run on local Emulator in headless mode

## Run from Command line

### Local run

```shell
mvn clean install -DdeviceName=<emulator_name> -DdeviceVersion=<emulator_version>
```

### LT Cloud run

```shell
export LT_USERNAME="your-cloud-user-name"
export LT_ACCESS_KEY="your-cloud-access-key"
export LT_APP_ANDROID="your-app-cloud-url"
mvn clean install -Dtarget=cloud -DdeviceName=<device_name> -DdeviceVersion=<device_version>
```