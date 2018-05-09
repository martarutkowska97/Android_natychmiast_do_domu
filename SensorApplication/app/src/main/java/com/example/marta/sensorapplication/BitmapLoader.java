package com.example.marta.sensorapplication;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by marta on 2018-04-27.
 */

public class BitmapLoader {

    private static final BitmapLoader ourInstance = new BitmapLoader();

    public static BitmapLoader getInstance() {
        return ourInstance;
    }

    private BitmapLoader() {
    }
    public Bitmap getBitmapFromAssets(String fileName, Context context){

        AssetManager assetManager = context.getAssets();
        InputStream is = null;
        try{
            is = assetManager.open(fileName);
        }catch(IOException e){
            e.printStackTrace();
        }

        Bitmap bitmap = BitmapFactory.decodeStream(is);
        return bitmap;
    }

}
