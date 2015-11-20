package mddemo.library.com.activityanimation_master;

import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author:  梁铖城
 * Email:   1038127753@qq.com
 * Date:    2015年11月21日00:32:39
 * Description:
 */
public class SqliteUtils {

    /**
     * 读取输入流中的文本
     * @param input  文件输入流
     * @param beginLine 开始的行
     * @param isLine 开始换行
     * @return 文本
     * @throws Exception 异常
     */
    public static String readFile(InputStream input,String encoding,int beginLine, boolean isLine) throws Exception {
        String result = "";
        beginLine = (beginLine <= 0 ? 1 : beginLine);
        encoding = (isBlank(encoding) ? HTTP.UTF_8 : encoding);
        InputStreamReader read = new InputStreamReader(input, encoding);
        BufferedReader reader = new BufferedReader(read);
        String line;
        int i = 1;
        while ((line = reader.readLine()) != null) {
            if (i >= beginLine) {
                result += line + (isLine ? "\n" : "");
            }
            i++;
        }
        reader.close();
        read.close();
        input.close();
        return result;
    }

    /**
     * 判断字符串是否为空串
     * @param str 操作的字符串
     * @return boolean
     */
    public static boolean isBlank(String str) {
        return null == str || str.trim().length() == 0;
    }

    /**
     * 复制文件
     * @param input 原来的文件流
     * @param newFile 新的文件
     * @throws Exception
     */
    public static void copyFile(InputStream input, File newFile) throws Exception {
        OutputStream output = new FileOutputStream(newFile);
        byte[] buffer = new byte[1024];
        int i = 0;
        while ((i = input.read(buffer)) != -1) {
            output.write(buffer, 0, i);
        }
        output.flush();
        output.close();
        input.close();
    }

    /**
     * 获得map的集合
     * @param sql sql语句
     * @param args   参数列表
     * @param keys   取值的key
     * @return List 结果集
     * @throws Exception
     */
    public static List<Map<String, Object>> getMaps(String sql, Object[] args, String[] keys) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (null == keys || keys.length == 0) {
            return list;
        }
        Connection connection = DriverManager.getConnection("jdbc:sqldroid:" + MainActivity.dbpath);
        Map<String, Object> map = executeSql(connection, sql, args);
        Statement statement = (Statement) map.get("statement");
        ResultSet resultSet = (ResultSet) map.get("resultSet");
        while (resultSet.next()) {
            map = new HashMap<String, Object>();
            for (int i = 0; i < keys.length; i++) {
                map.put(keys[i], resultSet.getString(keys[i]));
            }
            list.add(map);
        }
        close(connection, statement, resultSet);
        return list;
    }

    /**
     * 执行sql返回statement和resultSet
     * @param connection   连接
     * @param sql    sql语句
     * @param args   参数列表
     * @return Map 结果集
     * @throws Exception
     */
    public static Map<String, Object> executeSql(Connection connection, String sql, Object[] args) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Statement statement = null;
        ResultSet resultSet = null;
        if (null != args && args.length > 0) {
            statement = getPrepared(connection, sql, args);
            resultSet = ((PreparedStatement) statement).executeQuery();
        } else {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
        }
        map.put("statement", statement);
        map.put("resultSet", resultSet);
        return map;
    }

    /**
     * 获得预编译语句
     * @param connection   数据库连接
     * @param sql  sql语句
     * @param args   参数列表
     * @return PreparedStatement
     * @throws Exception
     */
    public static PreparedStatement getPrepared(Connection connection, String sql, Object[] args) throws Exception {
        PreparedStatement prepared = connection.prepareStatement(sql);
        if (null != args && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                prepared.setString(i + 1, null == args[i] ? "" : args[i].toString());
            }
        }
        return prepared;
    }

    /**
     * 关闭所有连接
     * @param connection  连接
     * @param statement  预编译语句
     * @param resultSet  结果集
     * @throws Exception
     */
    public static void close(Connection connection, Statement statement, ResultSet resultSet) throws Exception {
        if (null != resultSet) {
            resultSet.close();
            resultSet = null;
        }
        if (null != statement) {
            statement.close();
            statement = null;
        }
        if (null != connection && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
    }

    /**
     * 操作数据库(增删改)，返回受影响的数据行数
     * @param sql sql 语句
     * @param args 传递的参数
     * @return 返回结果
     * @throws Exception 抛出的异常
     */
    public static int manage(String sql, Object[] args) throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:sqldroid:" + MainActivity.dbpath);
        PreparedStatement prepared = getPrepared(connection, sql, args);
        int result = prepared.executeUpdate();
        close(connection, prepared, null);
        return result;
    }



}
