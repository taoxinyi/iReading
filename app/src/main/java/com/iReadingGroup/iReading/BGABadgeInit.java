package com.iReadingGroup.iReading;

import android.widget.TextView;

import com.lzy.widget.AlphaView;

import cn.bingoogolapple.badgeview.annotation.BGABadge;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:2018/1/14
 * 描述:初始化 BGABadgeView-Android
 * 1.在项目任意一个类上面添加 BGABadge 注解
 * 2.需要哪些类具有徽章功能，就把那些类的 class 作为 BGABadge 注解的参数
 * 3.再 AS 中执行 Build => Rebuild Project
 * 4.经过前面三个步骤后就可以通过「cn.bingoogolapple.badgeview.BGABadge原始类名」来使用徽章控件了
 */
@BGABadge({
        AlphaView.class,
        TextView.class//
})
public class BGABadgeInit {
}

