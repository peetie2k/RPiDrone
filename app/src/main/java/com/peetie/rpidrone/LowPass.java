package com.peetie.rpidrone;

/**
 * Created by Peetie_2 on 31.07.2015.
 */
public class LowPass {

    private float smoothFactor;
    private float smoothThreshold;
    private float smoothScale;
    private float oldValue = 0.0f;
    private float smoothedValue;


    public LowPass( float smoothFactor, float smoothThreshold, float smoothScale){
        this.smoothFactor = smoothFactor;
        this.smoothThreshold = smoothThreshold;
        this.smoothScale = smoothScale;
    }


    public float smooth(float newValue) {

        if (Math.abs(newValue - oldValue) < smoothThreshold) {
            if (Math.abs(newValue - oldValue) > smoothThreshold) {
                oldValue = newValue;
            } else {
                oldValue = oldValue + smoothFactor * (newValue - oldValue);
            }
        } else {
            if (smoothScale - Math.abs(newValue - oldValue) > smoothThreshold) {
                oldValue = newValue;
            } else {
                if (oldValue > newValue) {
                    oldValue = (oldValue + smoothFactor * ((smoothScale + newValue - oldValue) % smoothScale) + smoothScale) % smoothScale;
                } else {
                    oldValue = (oldValue - smoothFactor * ((smoothScale - newValue + oldValue) % smoothScale) + smoothScale) % smoothScale;
                }
            }
        }
        this.smoothedValue = oldValue;
        return  this.smoothedValue;

    }
}
