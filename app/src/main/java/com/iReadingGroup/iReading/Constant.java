package com.iReadingGroup.iReading;

/**
 * Created by xytao on 2018/4/29.
 */

public class Constant {
    public static final int SETTING_POLICY_ONLINE_FIRST = 0;
    public static final int SETTING_POLICY_OFFLINE_FIRST = 1;
    public static final int SETTING_POLICY_MOBILE_OFFLINE_FIRST = 2;
    public static final int SETTING_POLICY_OFFLINE_ALWAYS = 3;

    public static final String SETTING_REGISTER_URL = "https://eventregistry.org/register";
    public static final String[] SETTINGS_REFRESHING_LIST = {"1", "5", "10", "15", "20"};

    public static final String[] SETTINGS_LAUNCH_LIST = {"阅读", "查词", "收藏"};

    public static final String[] SETTINGS_POLICY_LIST = {"优先联网查询", "优先离线查询", "使用流量时优先离线查询","仅离线查询"};


    public static final int METHOD_FROM_ONLINE = 0;
    public static final int METHOD_FROM_OFFLINE = 1;

    public static final String URL_SEARCH_BRIEF_ICIBA = "https://dict-co.iciba.com/api/dictionary.php?key=341DEFE6E5CA504E62A567082590D0BD&type=json&w=";
    public static final String URL_SEARCH_ENTIRE_ICIBA = "https://dict-co.iciba.com/api/dictionary.php?key=341DEFE6E5CA504E62A567082590D0BD&w=";


}
