package wechat4j;

import org.apache.commons.lang.StringUtils;
import wechat4j.handler.IMenuHandler;
import wechat4j.handler.IUserGroupHandler;
import wechat4j.handler.IUserHandler;
import wechat4j.message.handler.IReplyMessageHandler;
import wechat4j.message.handler.MessageHandler;
import wechat4j.message.handler.MessageHandlerFactory;
import wechat4j.message.handler.ReplyMessageHandler;
import wechat4j.support.Configuration;
import wechat4j.support.ConfigurationBase;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

/**
 * AbstractWechat
 *
 * @author renbin.fang.
 * @date 2014/9/5.
 */
abstract class AbstractWechat implements Wechat {
    protected static MessageHandler commonMessageHandler;
    protected static MessageHandler eventMessageHandler;
    protected static IReplyMessageHandler replyMessageHandler;

    protected static IMenuHandler menuHandler;
    protected static IUserGroupHandler userGroupHandler;
    protected static IUserHandler userHandler;

    private static Configuration conf;
    private static final String packageName = "wechat4j.handler.impl";
    protected static Map<String, Object> objectMap = new HashMap<String, Object>();

    // 1st. 把handler对象都Put到map
    // 2nd. 获取本类中的field的handler接口
    // 3rd. set field 的值为handler对象
    //TODO 提供两种加载机制：eager, lazy
    static {
        conf = new ConfigurationBase();
        try {
            // 1st. 利用反射将实例化wechat4j.handler.impl下实现类
            Class[] classesInPackage = getClasses(packageName);
            Class[] classes = new Class<?>[classesInPackage.length];

            for (int i = 0; i < classesInPackage.length; i++) {
                classes[i] = classesInPackage[i];
            }

            // 2nd. 将conf对象注入到所有的handler
            for (Class clazz : classes) {
                Field field = clazz.getDeclaredField("conf");
                field.setAccessible(true);
                Object obj = clazz.newInstance();
                field.set(obj, conf);

                objectMap.put(getClassName(clazz.getName()).toUpperCase(), obj);
            }

            // 3rd. 将this抽象类中的handler接口都赋于实例
            Field[] fields = AbstractWechat.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName().toUpperCase();
                //除了Message Handler的接口赋值实例化后的对象
                if (!StringUtils.contains(fieldName, "MESSAGE") && StringUtils.contains(fieldName, "HANDLER")) {
                    field.set(fieldName, objectMap.get(fieldName.toUpperCase()));
                }
            }

            commonMessageHandler = MessageHandlerFactory.getMessageHandler("COMMON");
            eventMessageHandler = MessageHandlerFactory.getMessageHandler("EVENT");
            replyMessageHandler = new ReplyMessageHandler();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws java.io.IOException
     * @see [http://www.dzone.com/snippets/get-all-classes-within-package]
     */
    private static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }

        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     * @see [http://www.dzone.com/snippets/get-all-classes-within-package]
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }

        return classes;
    }

    /**
     * Get class name
     *
     * @param clazzName
     * @return
     */
    private static String getClassName(String clazzName) {
        String[] strings = clazzName.split("\\.");
        String value = null;
        if (strings.length > 0) {
            value = strings[strings.length - 1];
        }

        return value;
    }
}