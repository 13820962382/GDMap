package com.example.function.townicloudinncom.mapUtils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

/**
 * 异步根据URL获取图片
 * 修改加入本地缓存和软引用
 * @author Jackland_zgl
 *
 */
@SuppressLint("NewApi")
public class CacheUtils extends AsyncTask<Void, Void, Bitmap> {

        public static final String URL_KEY = "url";
        public static final String BITMAP_KEY = "bitmap";

        public static final String ImageCacheFilePath = "/sdcard/xiaomai/ImageCache/";
        private String url;
        private static HashMap<String, SoftReference<Bitmap>> cache;
        RefreshDelegate  refreshDelegate;

        /** 静态初始化软引用 */
        static {
            cache = new HashMap<String, SoftReference<Bitmap>>();
        }


        public CacheUtils(String url, RefreshDelegate rd) {
            this.url = url;
            this.refreshDelegate = rd;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            Bitmap bm;
            //1从软引用中取
            bm = getBitmapFromCache(url);
            if (bm!=null) {
//          Log.d("image","缓存");
                return bm;
            }

            //2从本地中取
            bm = getBitmapFromLocal(ImageCacheFilePath , modifyUriToFileName(url));
            if (bm!=null) {
                //放入缓存
                cache.put(url, new SoftReference<Bitmap>(bm));
//          Log.d("image","本地");
                return bm;
            }

            //3从网络取 ,若能取出则缓存
            bm = loadImageFromNet(url);
            if (bm!=null){
                try {
                    saveImageToSD(ImageCacheFilePath+modifyUriToFileName(url),bm);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                cache.put(url, new SoftReference<Bitmap>(bm));
//          Log.d("image","网络");
            }
            return bm;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            HashMap<String,Object> map = new HashMap<String,Object>();
            map.put(RefreshDelegate.KEY_URL, url);
            map.put(RefreshDelegate.KEY_BITMAP, result);
            if (refreshDelegate!=null)
                refreshDelegate.refresh(map);
        }

        /**
         * 从缓存中获取图片
         * @param url
         */
        public Bitmap getBitmapFromCache(String url) {
            Bitmap bitmap = null;
            if (cache.containsKey(url)) {
                bitmap = cache.get(url).get();
            }
            return bitmap;
        }

        /**
         * 从本地获取
         * @param path
         * @param url
         * @return
         */
        public Bitmap getBitmapFromLocal(String path,String url){
            return BitmapFactory.decodeFile(path+url);

        }

        /**
         * 从网络读取
         * @param url
         * @return
         */
        public static Bitmap loadImageFromNet(String url) {
            URL m;
            InputStream i = null;
            if (url == null) {
                return null;
            }
            try {
                m = new URL(url);

                i = (InputStream) m.getContent();
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            if (i == null) {
                return null;
            }
            return BitmapFactory.decodeStream(i);
        }

        /**
         * 写图片文件到SD卡
         *
         * @throws IOException
         */
        public static void saveImageToSD(String filePath,
                                         Bitmap bitmap) throws IOException {
            Log.d("image","存在本地:"+filePath);
            if (bitmap != null) {
                File file = new File(filePath.substring(0,
                        filePath.lastIndexOf(File.separator)));
                if (!file.exists()) {
                    file.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(filePath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                fos.write(stream.toByteArray());
                fos.close();
            }
        }

        /**
         * 修改URL里的斜杠
         * @param Url
         * @return
         */
        public static String modifyUriToFileName(String Url){
//      String mUrl = Url.replaceAll("/", "_").replaceAll("\\.", "-").replaceAll(":", "_")+".jpg";
            String mUrl = UUID.randomUUID().toString()+".png";
            return mUrl;
        }


        /**
         * 刷新代理
         * @author Jackland_zgl
         *
         */
        public interface RefreshDelegate{
            public static String KEY_URL="url";
            public static String KEY_BITMAP="bitmap";

            public int refresh(HashMap<String,Object> result);

        }
    }
