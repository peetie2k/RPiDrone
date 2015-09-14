package com.peetie.rpidrone;

/**
 * Created by Peetie_2 on 31.07.2015.
 */
public class CompassLowPass {
    private float SmoothFactorCompass = 0.15f;
    private float SmoothThresholdCompass = 180.0f;
    private float oldCompass = 0.0f;
    private float smoothedDegrees;

    public int smoothCompass(int newValue) {

        if (Math.abs(newValue - oldCompass) < 180) {
            if (Math.abs(newValue - oldCompass) > SmoothThresholdCompass) {
                oldCompass = newValue;
            } else {
                oldCompass = oldCompass + SmoothFactorCompass * (newValue - oldCompass);
            }
        } else {
            if (360.0 - Math.abs(newValue - oldCompass) > SmoothThresholdCompass) {
                oldCompass = newValue;
            } else {
                if (oldCompass > newValue) {
                    oldCompass = (oldCompass + SmoothFactorCompass * ((360 + newValue - oldCompass) % 360) + 360) % 360;
                } else {
                    oldCompass = (oldCompass - SmoothFactorCompass * ((360 - newValue + oldCompass) % 360) + 360) % 360;
                }
            }
        }
        this.smoothedDegrees = oldCompass;
        return (int) this.smoothedDegrees;

    }
}
