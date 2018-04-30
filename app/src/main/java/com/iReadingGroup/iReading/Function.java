package com.iReadingGroup.iReading;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.iReadingGroup.iReading.Bean.ArticleEntity;
import com.iReadingGroup.iReading.Bean.ArticleEntityDao;

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

    public static List<ArticleEntity> getCollectedArticlesList(ArticleEntityDao daoArticle, boolean status) {
        return daoArticle.queryBuilder().where(ArticleEntityDao.Properties.CollectStatus.eq(status)).list();

    }

    public static void clearAllUncollectedArticles(ArticleEntityDao daoArticle) {
        for (ArticleEntity articleEntity : getCollectedArticlesList(daoArticle, false))
            daoArticle.delete(articleEntity);
    }

}
