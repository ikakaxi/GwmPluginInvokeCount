/*
 * Created by renqingyou on 2018/12/01.
 * Copyright 2015－2022 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.liuhc.plugin

class TransformHelper {

    HashSet<String> ignoreClass = new HashSet<>(['keyboard'])
    HashSet<String> exclude = new HashSet<>(['android.support',
                                             'androidx',
                                             'com.qiyukf',
                                             'android.arch',
                                             'com.google.android',
                                             "com.tencent.smtt",
                                             "com.umeng.message",
                                             "com.xiaomi.push",
                                             "com.huawei.hms",
                                             "cn.jpush.android",
                                             "cn.jiguang",
                                             "com.meizu.cloud.pushsdk",
                                             "com.vivo.push",
                                             "com.igexin",
                                             "com.getui",
                                             "com.xiaomi.mipush.sdk",
                                             "com.heytap.msp.push",
                                             'com.bumptech.glide',
                                             'com.tencent.tinker'])
    HashSet<String> include = new HashSet<>([])
    /** 将一些特例需要排除在外 */
    public static final HashSet<String> special = []

    ClassNameAnalytics analytics(String className) {
        ClassNameAnalytics classNameAnalytics = new ClassNameAnalytics(className)
        if (!classNameAnalytics.isAndroidGenerated() && !classNameAnalytics.isKotlin() && !classNameAnalytics.isMETA_INF()) {
            for (pkgName in special) {
                if (className.startsWith(pkgName)) {
                    classNameAnalytics.isShouldModify = true
                    return classNameAnalytics
                }
            }

            classNameAnalytics.isShouldModify = true
            if (!classNameAnalytics.isLeanback()) {
                for (pkgName in exclude) {
                    if (className.startsWith(pkgName)) {
                        classNameAnalytics.isShouldModify = false
                        break
                    }
                }
                if (classNameAnalytics.isShouldModify) {
                    for (String ignore : ignoreClass) {
                        if (className.toLowerCase().contains(ignore)) {
                            classNameAnalytics.isShouldModify = false
                            break
                        }
                    }
                }
            }
        }
        return classNameAnalytics
    }

}

