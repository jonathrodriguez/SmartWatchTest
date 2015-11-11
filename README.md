# SmartWatchTest

Smartwatch heart rate sensor data retrieval and transfer to mobile.

---------------------------------------------------------------------

## Connect your phone to a virtual smartwatch.
Create and run a virtual smartwach device

### Pair your handheld with the emulator:
  1. On your handheld, install the Android Wear app from Google Play.
  2. Connect the handheld to your machine through USB.
  3. Forward the AVD's communication port to the connected handheld device (you must do this every time the handheld is connected):  
    ```adb -d forward tcp:5601 tcp:5601```
  4. Start the Android Wear app on your handheld device and connect to the emulator.
  5. Tap the menu on the top right corner of the Android Wear app and select **Demo Cards**.
  6. The cards you select appear as notifications on the home screen of the emulator.

### Enable debuging on your smartwach:
  1. Go to **Settings > About**.
  2. Tap **Build number** seven times.
  3. Swipe right to return to the Settings menu.
  4. Go to **Developer options** at the bottom of the screen.
  5. Tap **ADB Debugging** to enable adb.
  6. Connect the wearable to your machine through USB, so you can install apps directly to it as you develop. A message appears on both the wearable and the Android Wear app prompting you to allow debugging.  
    - **Note:** If you can not connect your wearable to your machine via USB, you can try connecting over Bluetooth.
  7. On the Android Wear app, check **Always allow from this computer** and tap **OK**.

### Connecting your smartwach over Bluetooth:
  1. Enable USB debugging on the handheld:
    - Open the Settings app and scroll to the bottom.
    - If it doesn't have a Developer Options setting, tap **About Phone** (or **About Tablet**), scroll to the bottom, and tap the build number 7 times.
    - Go back and tap **Developer Options**.
    - Enable **USB debugging**.
  2. Enable Bluetooth debugging on the wearable:
    - Tap the home screen twice to bring up the Wear menu.
    - Scroll to the bottom and tap **Settings**.
    - Scroll to the bottom. If there's no **Developer Options** item, tap **About**, and then tap the build number 7 times.
    - Tap the **Developer Options** item.
    - Enable **Debug over Bluetooth**.
  3. Set Up a Debugging Session
    - On the handheld, open the Android Wear companion app.
    - Tap the menu on the top right and select **Settings**.
    - Enable **Debugging over Bluetooth**. You should see a tiny status summary appear under the option:  
      ```
      Host: disconnected
      
      Target: connected
      ```
    - Connect the handheld to your machine over USB and run:  
      ```
      adb forward tcp:4444 localabstract:/adb-hub
      
      adb connect localhost:4444
      ```
      + **Note:** You can use any available port that you have access to.
    - In the Android Wear companion app, you should see the status change to:  
      ```
      Host: connected
      
      Target: connected
      ```
