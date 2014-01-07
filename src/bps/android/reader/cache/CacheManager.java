package bps.android.reader.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class CacheManager {
    
    private static final int PERCENT_OF_MEMORY_TO_USE_FOR_CACHE = 25;
    
    private static LruCache<String, Bitmap> memoryCacheForImage;
    
    public CacheManager(){
        memoryCacheForImage = new LruCache<String, Bitmap>(getCacheSize()){
            @Override
            protected int sizeOf(String key, Bitmap bitmap){
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }
    
    public static LruCache<String, Bitmap> getMemoryCacheForImage(){
        return memoryCacheForImage;
    }
    
    public static void setMemoryCacheForImage(LruCache<String, Bitmap> memoryCache){
        memoryCacheForImage = memoryCache;
    }
    
    public int getCacheSize(){
        //bytes
        int MaxMemory = (int)Runtime.getRuntime().maxMemory();
        // ## another way to get available memory (Mbytes)
        // int maxMemory = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        
        int cacheSize = (int)(MaxMemory * (PERCENT_OF_MEMORY_TO_USE_FOR_CACHE / 100.0));
        return cacheSize;
    }
}
