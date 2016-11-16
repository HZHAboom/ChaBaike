package com.hzh.chabaike.net;

import android.os.AsyncTask;

import com.hzh.chabaike.callback.ByteCallback;
import com.hzh.chabaike.utils.HttpUtils;

/**
 * Created by my on 2016/11/14.
 */
public class ByteAsyncTask extends AsyncTask<String,Void,byte[]>{
    private ByteCallback mByteCallback;

    public ByteAsyncTask(ByteCallback byteCallback) {
        mByteCallback = byteCallback;
    }

    @Override
    protected byte[] doInBackground(String... params) {
        byte[] ret = HttpUtils.loadBytes(params[0]);
        return ret;
    }

    @Override
    protected void onPostExecute(byte[] bytes) {
        super.onPostExecute(bytes);
        mByteCallback.callback(bytes);
    }
}
