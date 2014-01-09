package bps.android.reader.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class CacheManager {
    
    private static final int PERCENT_OF_MEMORY_TO_USE_FOR_CACHE = 25;
    
    private static LruCache<String, Bitmap> sMemoryCacheForImage;
    
    private static CacheManager sInstance;
    
    public static CacheManager getInstance(){
        if (sInstance == null){
            sInstance = new CacheManager();
        }
        if (sMemoryCacheForImage == null){
            sMemoryCacheForImage = new LruCache<String, Bitmap>(getCacheSize()){
                @Override
                protected int sizeOf(String key, Bitmap bitmap){
                    return bitmap.getRowBytes() * bitmap.getHeight();
                }
            };
        }
        return sInstance;
    }
    
    public LruCache<String, Bitmap> getMemoryCacheForImage(){
        return sMemoryCacheForImage;
    }
    
    public void setMemoryCacheForImage(LruCache<String, Bitmap> memoryCache){
        sMemoryCacheForImage = memoryCache;
    }
    
    private static int getCacheSize(){
        //bytes
        int MaxMemory = (int)Runtime.getRuntime().maxMemory();
        // ## another way to get available memory (Mbytes)
        // int maxMemory = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        
        int cacheSize = (int)(MaxMemory * (PERCENT_OF_MEMORY_TO_USE_FOR_CACHE / 100.0));
        return cacheSize;
    }
    
    public static void clear(){
        sMemoryCacheForImage.evictAll();
    }
}
