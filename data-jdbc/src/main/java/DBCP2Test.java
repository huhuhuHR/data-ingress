import org.apache.commons.dbcp2.BasicDataSource;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DBCP2Test {
    public static void main(String[] args) throws Exception {
        // https://www.iteye.com/blog/bsr1983-2092467
        // 通过JDBC建立一个连接所需的URL
        String DATABASE_URL = "jdbc:mysql://192.168.42.6:3306/datax";
        // 所使用的JDBC驱动的类全名
        String DB_DRIVERNAME = "com.mysql.jdbc.Driver";
        // JDBC 驱动路径
        String DB_DRIVER_LOCATION = "mysql-connector-java-5.1.44.jar";
        // 通过JDBC建立一个连接所需的用户名
        String DB_USER = "root";
        // 	通过JDBC建立一个连接所需的密码
        String DB_PASSWORD = "123456";
        // （如果没有可用连接）池在抛出异常前等待的一个连接被归还的最大毫秒数，设置为-1则等待时间不确定
        String MAX_WAIT_TIME = "500";
        // 可以在这个池中同时被分配的有效连接数的最大值，如设置为负数，则不限制
        String MAX_TOTAL_CONNECTIONS = "8";
        // 在连接池返回连接给调用者前用来进行连接校验的查询sql。如果指定，则这个查询必须是一个至少返回一行数据的SQL SELECT语句。如果没有指定，则连接将通过调用isValid() 方法进行校验
        String VALIDATION_QUERY = "select 1";
        // 可以在池中保持空闲的最小连接数，超出设置值之外的空闲连接将被创建，如设置为0，则不创建
        String MIN_IDLE = "0";
        // 可以在池中保持空闲的最大连接数，超出设置值之外的空闲连接将被回收，如设置为负数，则不限制
        String MAX_IDLE = "8";
        // 一个连接的最大存活毫秒数。如果超过这个时间，则连接在下次激活、钝化、校验时都将会失败。如果设置为0或小于0的值，则连接的存活时间是无限的。
        String MAX_CONN_LIFETIME = "-1";
        // 空闲对象驱逐线程运行时的休眠毫秒数，如果设置为非正数，则不运行空闲对象驱逐线程。
        String EVICTION_RUN_PERIOD = "-1";
        // 符合对象驱逐对象驱逐条件的对象在池中最小空闲毫秒总数（如果有的话）
        String MIN_EVICTABLE_IDLE_TIME = "30";
        // 符合对象驱逐对象驱逐条件的对象在池中最小空闲毫秒总数，额外的条件是池中至少保留有minIdle所指定的个数的连接。当miniEvictableIdleTimeMillis 被设置为一个正数，空闲连接驱逐者首先检测miniEvictableIdleTimeMillis，当空闲连接被驱逐者访问时，首先与miniEvictableIdleTimeMillis 所指定的值进行比较（而不考虑当前池中的空闲连接数），然后比较softMinEvictableIdleTimeMillis所指定的连接数，包括minIdle条件。
        String SOFT_MIN_EVICTABLE_IDLE_TIME = "-1";
        BasicDataSource dataSource = null;
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            dataSource = new BasicDataSource();
            dataSource.setDriverClassName(DB_DRIVERNAME);
            dataSource.setDriverClassLoader(new DBCP2Test().getDriverClassLoader(DB_DRIVER_LOCATION, DB_DRIVERNAME));
            dataSource.setMaxWaitMillis(Long.parseLong(MAX_WAIT_TIME));
            dataSource.setMaxTotal(Integer.parseInt(MAX_TOTAL_CONNECTIONS));
            dataSource.setMinIdle(Integer.parseInt(MIN_IDLE));
            dataSource.setMaxIdle(Integer.parseInt(MAX_IDLE));
            dataSource.setMaxConnLifetimeMillis(Long.parseLong(MAX_CONN_LIFETIME));
            dataSource.setTimeBetweenEvictionRunsMillis(Long.parseLong(EVICTION_RUN_PERIOD));
            dataSource.setMinEvictableIdleTimeMillis(Long.parseLong(MIN_EVICTABLE_IDLE_TIME) * 60 * 1000);
            dataSource.setSoftMinEvictableIdleTimeMillis(Long.parseLong(SOFT_MIN_EVICTABLE_IDLE_TIME));
            dataSource.setValidationQuery(VALIDATION_QUERY);
            dataSource.setTestOnBorrow(true);
            dataSource.setUrl(DATABASE_URL);
            dataSource.setUsername(DB_USER);
            dataSource.setPassword(DB_PASSWORD);
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "SELECT * from achievement_source LIMIT 1";
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                String gender = rs.getString(3);
                System.out.println("id:" + id + " 姓名：" + name + " 性别：" + gender);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            try {
                if (dataSource != null) {
                    dataSource.close();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public ClassLoader getDriverClassLoader(String locationString, String drvName) throws Exception {
        if (locationString != null && locationString.length() > 0) {
            try {
                final ClassLoader classLoader = getCustomClassLoader(
                        locationString,
                        this.getClass().getClassLoader(),
                        (dir, name) -> name != null && name.endsWith(".jar")
                );
                final Class<?> clazz = Class.forName(drvName, true, classLoader);
                if (clazz == null) {
                    System.out.println("Can't load Database Driver " + drvName);
                }
                final Driver driver = (Driver) clazz.newInstance();
                DriverManager.registerDriver(new DriverShim(driver));
                return classLoader;
            } catch (final MalformedURLException e) {
                System.out.println("Invalid Database Driver Jar Url" + e.getMessage());
                throw e;
            } catch (final Exception e) {
                System.out.println("Can't load Database Driver" + e.getMessage());
                throw e;
            }
        } else {
            return Thread.currentThread().getContextClassLoader();
        }
    }

    public static ClassLoader getCustomClassLoader(String modulePath, ClassLoader parentClassLoader, FilenameFilter filenameFilter) throws MalformedURLException {
        URL[] classpaths = getURLsForClasspath(modulePath, filenameFilter, false);
        return createModuleClassLoader(classpaths, parentClassLoader);
    }


    public static URL[] getURLsForClasspath(String modulePath, FilenameFilter filenameFilter, boolean suppressExceptions) throws MalformedURLException {
        return getURLsForClasspath(modulePath == null ? Collections.emptySet() : Collections.singleton(modulePath), filenameFilter, suppressExceptions);
    }


    public static URL[] getURLsForClasspath(Set<String> modulePaths, FilenameFilter filenameFilter, boolean suppressExceptions) throws MalformedURLException {
        // use LinkedHashSet to maintain the ordering that the incoming paths are processed
        Set<String> modules = new LinkedHashSet<>();
        if (modulePaths != null) {
            modulePaths.stream()
                    .flatMap(path -> Arrays.stream(path.split(",")))
                    .filter(path -> isNotBlank(path))
                    .map(String::trim)
                    .forEach(m -> modules.add(m));
        }
        return toURLs(modules, filenameFilter, suppressExceptions);
    }

    private static boolean isNotBlank(final String value) {
        return value != null && !value.trim().isEmpty();
    }

    protected static URL[] toURLs(Set<String> modulePaths, FilenameFilter filenameFilter, boolean suppressExceptions) throws MalformedURLException {
        List<URL> additionalClasspath = new LinkedList<>();
        if (modulePaths != null) {
            for (String modulePathString : modulePaths) {
                boolean isUrl = true;
                try {
                    additionalClasspath.add(new URL(modulePathString));
                } catch (MalformedURLException mue) {
                    isUrl = false;
                }
                if (!isUrl) {
                    try {
                        File modulePath = new File(modulePathString);

                        if (modulePath.exists()) {

                            additionalClasspath.add(modulePath.toURI().toURL());

                            if (modulePath.isDirectory()) {
                                File[] files = modulePath.listFiles(filenameFilter);

                                if (files != null) {
                                    for (File classpathResource : files) {
                                        if (classpathResource.isDirectory()) {
                                            System.out.println("Recursive directories are not supported, skipping " + classpathResource.getAbsolutePath());
                                        } else {
                                            additionalClasspath.add(classpathResource.toURI().toURL());
                                        }
                                    }
                                }
                            }
                        } else {
                            throw new MalformedURLException("Path specified does not exist");
                        }
                    } catch (MalformedURLException e) {
                        if (!suppressExceptions) {
                            throw e;
                        }
                    }
                }
            }
        }
        return additionalClasspath.toArray(new URL[additionalClasspath.size()]);
    }

    public static String generateAdditionalUrlsFingerprint(Set<URL> urls) {
        List<String> listOfUrls = urls.stream().map(Object::toString).collect(Collectors.toList());
        StringBuffer urlBuffer = new StringBuffer();

        Collections.sort(listOfUrls);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            listOfUrls.forEach(url -> {
                urlBuffer.append(url).append("-").append(getLastModified(url)).append(";");
            });
            byte[] bytesOfAdditionalUrls = urlBuffer.toString().getBytes("UTF-8");
            byte[] bytesOfDigest = md.digest(bytesOfAdditionalUrls);

            return DatatypeConverter.printHexBinary(bytesOfDigest);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            System.out.println("Unable to generate fingerprint for the provided additional resources {}" + Arrays.toString(new Object[]{urls, e}));
            return null;
        }
    }

    private static long getLastModified(String url) {
        File file = null;
        try {
            file = new File(new URI(url));
        } catch (URISyntaxException e) {
            System.out.println("Error getting last modified date for " + url);
        }
        return file != null ? file.lastModified() : 0;
    }

    protected static ClassLoader createModuleClassLoader(URL[] modules, ClassLoader parentClassLoader) {
        return new URLClassLoader(modules, parentClassLoader);
    }

    static class DriverShim implements Driver {
        private Driver driver;

        DriverShim(Driver d) {
            this.driver = d;
        }

        @Override
        public boolean acceptsURL(String u) throws SQLException {
            return this.driver.acceptsURL(u);
        }

        @Override
        public Connection connect(String u, Properties p) throws SQLException {
            return this.driver.connect(u, p);
        }

        @Override
        public int getMajorVersion() {
            return this.driver.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return this.driver.getMinorVersion();
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
            return this.driver.getPropertyInfo(u, p);
        }

        @Override
        public boolean jdbcCompliant() {
            return this.driver.jdbcCompliant();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return driver.getParentLogger();
        }

    }
}
