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
package com.gwm.plugin

class TransformHelper {

    HashSet<String> ignoreClass = new HashSet<>(['keyboard'])
    HashSet<String> exclude = new HashSet<>(['android.support',
                                             'androidx',
                                             'android.arch',
                                             'com.google.android',
                                             "org.intellij",
                                             "kotlin",
                                             "META-INF",
                                             "org.jetbrains"])

    ClassNameAnalytics analytics(String className) {
        ClassNameAnalytics classNameAnalytics = new ClassNameAnalytics(className)
        if (!classNameAnalytics.isAndroidGenerated()) {
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

