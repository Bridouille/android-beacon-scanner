package com.bridou_n.beaconscanner.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.bridou_n.beaconscanner.utils.copyPaste.Base64;
import com.bridou_n.beaconscanner.utils.copyPaste.Base91;
import com.bridou_n.beaconscanner.utils.extensionFunctions.IntArraysKt;

/**
 * Created by bridou_n on 05/09/2017.
 */

public class RuuviParser {
    private String TAG = "RuuviParser";

    private int BASE_64 = 0;
    private int BASE_91 = 1;

    private float humidity = 0;
    private float airPressure = 0;
    private float temp = 0;

    private int beaconId = -1;

    public RuuviParser(@NonNull String ruuviUrl) {
        if (ruuviUrl.length() < 6) {
            return ;
        }

        // Zero-pad URL if needed - example: AIgbAMLNs
        if (ruuviUrl.length() == 9) {
            ruuviUrl += "...";
        }

        int[] decoded = getDecodedArray(ruuviUrl.getBytes(), BASE_91);
        int[] decoded64 = getDecodedArray(ruuviUrl.getBytes(), BASE_64);

        int format = decoded64[0];

        Log.d(TAG, IntArraysKt.print(decoded));
        Log.d(TAG, IntArraysKt.print(decoded64));
        Log.d(TAG, "format: " + format);

        if (format != 2 && format != 4) {
            /*
            ** 0:   uint8_t     format;          // (0x01 = realtime sensor readings in base91)
            ** 1:   uint8_t     humidity;        // one lsb is 0.5%
            ** 2-3: uint16_t    temperature;     // Signed 8.8 fixed-point notation.
            ** 4-5: uint16_t    pressure;        // (-50kPa)
            ** 6-7: uint16_t    time;            // seconds (now from reset, later maybe from last movement)
            */
            humidity = decoded[1] * 0.5F;

            int uTemp = (((decoded[3] & 127) << 8) | decoded[2]);
            int tempSign = (decoded[3] >> 7) & 1;

            temp = tempSign == 0 ? uTemp / 256F : -1 * uTemp / 256F;
            airPressure = ((decoded[5] << 8) + decoded[4]) + 50000;
        } else {
            /*
             ** 0:   uint8_t     format;          // (0x02 = realtime sensor readings in base64)
             ** 1:   uint8_t     humidity;        // one lsb is 0.5%
             ** 2-3: uint16_t    temperature;     // Signed 8.8 fixed-point notation.
             ** 4-5: uint16_t    pressure;        // (-50kPa)
              */
            humidity = decoded64[1] * 0.5F;

            int uTemp = (((decoded64[2] & 127) << 8) | decoded64[3]);
            int tempSign = (decoded64[2] >> 7) & 1;

            temp = tempSign == 0 ? uTemp / 256F : -1 * uTemp / 256F;
            airPressure = ((decoded64[4] << 8) + decoded64[5]) + 50000;

            // example: BIgbAMLNsN 27/81 C/F 68%  999Pa ID=N
            if (ruuviUrl.charAt(0) == 'B' && ruuviUrl.length() > 8) {
                beaconId = ruuviUrl.charAt(8);
            }
        }
    }

    private int[] getDecodedArray(byte[] ruuviUrl, int decodingType) {
        byte[] decodedTmp = decodingType == BASE_64 ? Base64.decode(ruuviUrl) : Base91.decode(ruuviUrl);
        int[] decoded = new int[decodedTmp.length];

        for (int i = 0; i < decodedTmp.length; i++) {
            decoded[i] = (int)decodedTmp[i] & 0xFF;
        }

        return decoded;
    }

    public int getHumidity() {
        return Math.round(humidity); // %
    }

    public int getAirPressure() {
        return Math.round(airPressure / 100); // hPa
    }

    public int getTemp() {
        return Math.round(temp); // C°
    }

    public int getBeaconId() {
        return Math.round(beaconId);
    }
}
