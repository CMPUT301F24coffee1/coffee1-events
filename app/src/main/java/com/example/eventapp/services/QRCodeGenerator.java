package com.example.eventapp.services;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

/**
 * The `QRCodeGenerator` class is responsible for generating a QR code bitmap
 * using the given data and dimensions. It provides methods to generate the QR code
 * and access or modify the data, dimensions, and generated bitmap.
 */
public class QRCodeGenerator {
    private String data;
    private int width;
    private int height;
    private Bitmap qrCodeBitmap;

    /**
     * Constructs a QRCodeGenerator instance with the specified data, width, and height.
     *
     * @param data   The data to encode in the QR code.
     * @param width  The width of the QR code bitmap.
     * @param height The height of the QR code bitmap.
     */
    public QRCodeGenerator(String data, int width, int height){
        this.data = data;
        this.width = width;
        this.height = height;
    }

    /**
     * Generates a QR code bitmap using the current data, width, and height.
     * The generated bitmap is stored in the `qrCodeBitmap` property.
     */
    public void generateQRCodeBitmap(){
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            this.qrCodeBitmap = barcodeEncoder.encodeBitmap(this.data, BarcodeFormat.QR_CODE, this.width, this.height);
        } catch (Exception e){
            Log.d("QRCodeGenerator", "Failed to generate QR Code bitmap");
        }
    }

    /**
     * Returns the data currently set for the QR code.
     *
     * @return The data to be encoded in the QR code.
     */
    public String getData() {
        return data;
    }

    /**
     * Updates the data to encode in the QR code.
     *
     * @param data The new data to be encoded.
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Returns the width of the QR code bitmap.
     *
     * @return The width of the QR code bitmap.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Updates the width of the QR code bitmap.
     *
     * @param width The new width of the QR code bitmap.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Returns the height of the QR code bitmap.
     *
     * @return The height of the QR code bitmap.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Updates the height of the QR code bitmap.
     *
     * @param height The new height of the QR code bitmap.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Returns the generated QR code bitmap.
     *
     * @return The generated QR code bitmap, or null if it has not been generated yet.
     */
    public Bitmap getQrCodeBitmap() {
        return qrCodeBitmap;
    }

    /**
     * Sets the QR code bitmap manually.
     *
     * @param qrCodeBitmap The new QR code bitmap to set.
     */
    public void setQrCodeBitmap(Bitmap qrCodeBitmap) {
        this.qrCodeBitmap = qrCodeBitmap;
    }
}
