package com.iReadingGroup.iReading;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import com.iReadingGroup.iReading.Bean.ArticleEntity;
import com.iReadingGroup.iReading.Bean.ArticleEntityDao;
import com.iReadingGroup.iReading.Bean.OfflineDictBean;
import com.iReadingGroup.iReading.Bean.OfflineDictBeanDao;
import com.iReadingGroup.iReading.Bean.WordCollectionBeanDao;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by xytao on 2018/4/30.
 * class for shared function
 */

public class Function {
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isMobileConnected(Context context) {
        ConnectivityManager cm;
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;
    }

    public static List<ArticleEntity> getCollectedArticlesList(ArticleEntityDao daoArticle, boolean status) {
        return daoArticle.queryBuilder().where(ArticleEntityDao.Properties.CollectStatus.eq(status)).list();
    }

    public static boolean getArticleCollectedStatus(ArticleEntityDao daoArticle, String uri) {
        return getArticleEntity(daoArticle, uri).getCollectStatus();
    }

    public static ArticleEntity getArticleEntity(ArticleEntityDao daoArticle, String uri) {
        return daoArticle.queryBuilder().where(ArticleEntityDao.Properties.Uri.eq(uri)).list().get(0);

    }

    public static String getWordOfflineMeaning(OfflineDictBeanDao daoDictionary, String word) {
        List<OfflineDictBean> l = daoDictionary.queryBuilder()
                .where(OfflineDictBeanDao.Properties.Word.eq(word))
                .list();
        return (l.size() > 0) ? l.get(0).getMeaning() : null;
    }

    public static String getWordOfflineSentence(OfflineDictBeanDao daoDictionary, String word) {
        List<OfflineDictBean> l = daoDictionary.queryBuilder()
                .where(OfflineDictBeanDao.Properties.Word.eq(word))
                .list();
        return (l.size() > 0) ? l.get(0).getSentence() : null;
    }

    public static boolean getWordOfflineStatus(OfflineDictBeanDao daoDictionary, String word) {
        List<OfflineDictBean> l = daoDictionary.queryBuilder()
                .where(OfflineDictBeanDao.Properties.Word.eq(word))
                .list();
        return l.size() > 0;
    }

    public static boolean getWordCollectionStatus(WordCollectionBeanDao daoCollection, String word) {
        return daoCollection.queryBuilder().where(WordCollectionBeanDao.Properties.Word.eq(word)).list().size() != 0;

    }

    public static void clearAllUncollectedArticles(ArticleEntityDao daoArticle) {
        for (ArticleEntity articleEntity : getCollectedArticlesList(daoArticle, false))
            daoArticle.delete(articleEntity);
    }

    public static MyApplication getMyApplication(Context context) {
        return (MyApplication) context.getApplicationContext();
    }

    public static String getDefaultRequestUrl(String count, String apiKey) {
        return "http://eventregistry.org/json/article?" +
                "lang=eng&action=getArticles&resultType=articles&articlesSortBy=date&" +
                "articlesCount=" + count +
                "&articlesIncludeArticleEventUri=false&" +
                "articlesIncludeArticleImage=true&" +
                "apiKey=" + apiKey +
                "&articlesArticleBodyLen=0&articlesIncludeConceptLabel=false";
    }

    public static String getSourceRequestUrl(String source, String count, String apiKey) {
        return "http://eventregistry.org/json/article?sourceUri=" +
                source +
                "&lang=eng" +
                "&action=getArticles&" +
                "resultType=articles&" +
                "articlesSortBy=date&" +
                "articlesCount=" + count +
                "&articlesIncludeArticleEventUri=false&" +
                "articlesIncludeArticleImage=true&" +
                "articlesArticleBodyLen=0&" +
                "apiKey=" + apiKey +
                "&articlesIncludeConceptLabel=false";
    }

    public static String getArticleDetailUrl(String uri, String apiKey) {
        return "http://eventregistry.org/json/article?action=getArticle&" +
                "resultType=info&infoIncludeArticleBasicInfo=false&infoIncludeArticleEventUri=false&" +
                "infoIncludeArticleCategories=true&infoArticleBodyLen=-1&infoIncludeArticleImage=true&" +
                "infoIncludeConceptLabel=false&" +
                "apiKey=" + apiKey +
                "&articleUri=" + uri;
    }

    public static String getWordDetailUrl(String word) {
        return Constant.URL_SEARCH_ENTIRE_ICIBA + word;
    }

    public static String getWordBriefUrl(String word) {
        return Constant.URL_SEARCH_BRIEF_ICIBA + word;
    }

    /**
     * Gets total cache size.
     *
     * @param context the context
     * @return total cache size
     * @throws Exception 获取当前缓存
     */
    public static String getTotalCacheSize(Context context) {
        try {
            long cacheSize = getFolderSize(context.getCacheDir());
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                cacheSize += getFolderSize(context.getExternalCacheDir());
            }
            return getFormatSize(cacheSize);
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }

    }

    /**
     * Clear all cache.
     *
     * @param context 删除缓存
     */
    public static void clearAllCache(Context context) {
        deleteDir(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            deleteDir(context.getExternalCacheDir());
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            int size = 0;
            if (children != null) {
                size = children.length;
                for (int i = 0; i < size; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }

        }
        if (dir == null) {
            return true;
        } else {

            return dir.delete();
        }
    }

    /**
     * Gets folder size.
     *
     * @param file the file
     * @return the folder size
     * @throws Exception the exception
     */
// 获取文件
    // Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/
    // 目录，一般放一些长时间保存的数据
    // Context.getExternalCacheDir() -->
    // SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
    private static long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            int size2 = 0;
            if (fileList != null) {
                size2 = fileList.length;
                for (int i = 0; i < size2; i++) {
                    // 如果下面还有文件
                    if (fileList[i].isDirectory()) {
                        size = size + getFolderSize(fileList[i]);
                    } else {
                        size = size + fileList[i].length();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 格式化单位
     * 计算缓存的大小
     *
     * @param size the size
     * @return format size
     */
    private static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            // return size + "Byte";
            return "0K";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }

    public static String getHtml(String s) {
        return "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + s +
                "<script>\n" +
                "       var myHref = document.getElementsByTagName(\"a\");\n" +
                "        for (var i = 0,mylength = myHref.length; i<mylength; i++) {\n" +
                "    (function(i){  //这里的i跟外部的i实际不是一个i\n" +
                "        myHref[i].addEventListener(\"click\",function(e){\n" +
                "\t\t\tvar rect = myHref[i].getBoundingClientRect();\n" +
                "\t\t\tAndroid.showMenu((rect.left+rect.right)/2, rect.top + rect.height);\n" +
                "        },\"false\");\n" +
                "    })(i);\n" +
                "}\n" +
                "</script>";
    }


}
