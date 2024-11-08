package com.example.eventapp.services;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeGenerator {
    private String data;
    private int width;
    private int height;
    private Bitmap qrCodeBitmap;

    public QRCodeGenerator(String data, int width, int height){
        this.data = data;
        this.width = width;
        this.height = height;
    }

    // not yet used - ignore
    public void generateQRCodeBitmap(){
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            this.qrCodeBitmap = barcodeEncoder.encodeBitmap(this.data, BarcodeFormat.QR_CODE, this.width, this.height);
        } catch (Exception e){
            Log.d("QRCodeGenerator", "Failed to generate QR Code bitmap");
        }
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Bitmap getQrCodeBitmap() {
        return qrCodeBitmap;
    }

    public void setQrCodeBitmap(Bitmap qrCodeBitmap) {
        this.qrCodeBitmap = qrCodeBitmap;
    }
}
