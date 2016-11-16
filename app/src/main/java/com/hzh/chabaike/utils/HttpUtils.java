package com.hzh.chabaike.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by my on 2016/11/14.
 */
public class HttpUtils {

    public static byte[] loadBytes(String path) {
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            if(conn.getResponseCode()==200){
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024*8];
                int len = 0;
                while((len=is.read(buf))!=-1){
                    baos.write(buf,0,len);
                }
                return baos.toByteArray();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
