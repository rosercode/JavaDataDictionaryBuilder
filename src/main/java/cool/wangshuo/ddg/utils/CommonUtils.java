package cool.wangshuo.ddg.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author
 * @description
 * @createDate 2022/11/10 10:00
 */

public class CommonUtils {

    /**
     * 下载文件到本地
     * @param url 网络资源
     * @param path 本地路径
     */
    public static void download(URL url, String path){


    }

    /**
     * 从文件中加载 jar 包
     * @param file
     */
    public static void loadDriver(File file) {
        try {
            loadDriver(file.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从网络从加载 jar 包
     * @param url
     */
    public static void loadDriver(URL url){
        URLClassLoader classloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method add = null;
        try {
            add = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            add.setAccessible(true);
            add.invoke(classloader, new Object[]{url});
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
