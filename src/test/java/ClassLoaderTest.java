import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ClassLoaderTest {


    //@Test
    //public void test() {
    //    System.out.println("\"hello\" = " + "hello");
    //}

    @Test
    public void testClassLoader() throws ClassNotFoundException, SQLException {
        ClassLoader classLoader = ClassLoaderTest.class.getClassLoader();
        System.out.println("classLoader = " + classLoader);

        ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
        System.out.println("threadClassLoader = " + threadClassLoader);

        Class<?>  mysql_5_class = Class.forName("com.mysql.jdbc.Driver");
        ClassLoader mysql_5_classClassLoader = mysql_5_class.getClassLoader();
        System.out.println("mysql_5_classClassLoader = " + mysql_5_classClassLoader);


        //Class<?> mysql_8_class = Class.forName("com.mysql.cj.jdbc.Driver");
        //ClassLoader mysql_8_classClassLoader = mysql_8_class.getClassLoader();
        //System.out.println("mysql_8_classClassLoader = " + mysql_8_classClassLoader);

        String url = "jdbc:mysql://192.168.187.100:3306/test";
        Connection connection = DriverManager.getConnection(url);
    }


    @Test
    public void test3() throws Exception {
        // MySQL 5 和 MySQL 8 JAR 文件的路径
        String mysql5JarPath = "C:\\Users\\17692\\.m2\\repository\\mysql\\mysql-connector-java\\5.1.10\\mysql-connector-java-5.1.10.jar";
        String mysql8JarPath = "C:\\Users\\17692\\.m2\\repository\\com\\mysql\\mysql-connector-j\\8.0.33\\mysql-connector-j-8.0.33.jar";

        // 创建两个自定义类加载器
        URLClassLoader mysql5ClassLoader = createClassLoader(mysql5JarPath);
        URLClassLoader mysql8ClassLoader = createClassLoader(mysql8JarPath);

        // 测试加载类
        //testClassLoader(mysql5ClassLoader, "com.mysql.jdbc.Driver");
        //testClassLoader(mysql8ClassLoader, "com.mysql.cj.jdbc.Driver");

        Class<?> mysql_5_class = Class.forName("com.mysql.jdbc.Driver", true, mysql5ClassLoader);
        Class<?> mysql_8_class = Class.forName("com.mysql.cj.jdbc.Driver", true, mysql8ClassLoader);

        mysql5ClassLoader.loadClass("com.mysql.jdbc.Driver");

        String url = "jdbc:mysql://192.168.187.100:3306/test";
        Driver driver = (Driver) mysql_8_class.getDeclaredConstructor().newInstance();

        java.util.Properties info = new java.util.Properties();
        Connection connection = driver.connect(url, info);
    }

    private static URLClassLoader createClassLoader(String jarPath) throws IOException {
        // 创建一个 URL 数组来存放 JAR 文件的位置
        URL[] urls = { new File(jarPath).toURI().toURL() };
        // 创建 URLClassLoader 并返回
        return new URLClassLoader(urls);
    }

    private static void testClassLoader(URLClassLoader classLoader, String className) {
        try {
            // 使用自定义类加载器加载类
            Class<?> driverClass = classLoader.loadClass(className);
            System.out.println("Successfully loaded: " + driverClass.getName() + "classLoader: " + classLoader);
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + className);
            e.printStackTrace();
        }
    }
}
