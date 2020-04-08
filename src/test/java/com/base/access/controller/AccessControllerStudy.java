package com.base.access.controller;

import java.io.File;
import java.io.IOException;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author zhaoxuedui <zhaoxuedui@kuaishou.com>
 * Created on 2020-04-06
 * @Description <a href="https://www.jianshu.com/p/81985bc2bfa3"></a>
 * <p>
 * java -Djava.security.policy=.\\MyPolicy.txt -classpath
 * /Users/zhaoxuedui/IdeaProjects com.base.access.controller.TestStudy
 */
public class AccessControllerStudy {
    // 工程 A 执行文件的路径
    private final static String FOLDER_PATH = "/Users/zhaoxuedui/IdeaProjects";

    public static void makeFile(String fileName) {
        try {
            // 尝试在工程 A 执行文件的路径中创建一个新文件
            File fs = new File(FOLDER_PATH + java.io.File.separator + fileName);
            fs.createNewFile();
        } catch (AccessControlException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void doPrivilegedAction(final String fileName) {
        // 用特权访问方式创建文件
        AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                makeFile(fileName);
                return null;
            }
        });
    }

}
