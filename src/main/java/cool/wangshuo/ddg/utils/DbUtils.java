package cool.wangshuo.ddg.utils;

import cool.wangshuo.ddg.model.Column;
import cool.wangshuo.ddg.model.DbConfig;
import cool.wangshuo.ddg.model.Index;
import cool.wangshuo.ddg.model.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.sql.*;
import java.util.*;

/**
 * @author wangshuo
 * @description
 * @createDate 2022/11/10 12:41
 * @link https://www.jianshu.com/p/b67837ad9900
 */
@Slf4j
public class DbUtils {

    private static final String MYSQL = "MYSQL";
    private static final String ORACLE = "ORACLE";
    private static final String HANA = "HANA";
    private static final String SQLSERVER = "SQLSERVER";
    private static final String H2 = "H2";
    private static final String POSTGRESQL = "POSTGRESQL";
    private static final String ZENITH = "ZENITH";
    private static final String SCHEMA = "";

    private DbUtils() {
    }


    // 获取数据连接
    public static List<String> getDBList(Connection connection) throws Exception {
        List<String> dbList = new ArrayList<>();

        String dbType = getDbType(connection.getMetaData().getURL());
        ResultSet rs = null;
        switch (dbType) {
            case POSTGRESQL:
                Statement stmt = connection.createStatement();
                rs = stmt.executeQuery("SELECT datname FROM pg_database WHERE datistemplate = false;");
                while (rs.next()) {
                    dbList.add(rs.getString(1));
                }
                break;
            default:
                DatabaseMetaData databaseMetaData = connection.getMetaData();
                rs = databaseMetaData.getCatalogs();
                while (rs.next()) {
                    dbList.add(rs.getString("TABLE_CAT"));
                }
                break;
        }
        if (rs != null) {
            rs.close();
        }
        return dbList;
    }

    // 获取表列表
    public static List<Table> getTables(DbConfig config) {
        List<Table> list = new ArrayList<>();
        try (Connection connection = getConnection(config)) {
            return getTables(connection);
        } catch (Exception e) {
            return list;
        }
    }

    public static List<Table> getTables(Connection connection) {
        try {
            return getTables(connection.getMetaData());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static List<Table> getTables(DatabaseMetaData metaData) {
        List<Table> list = new ArrayList<>();
        try {
            if (metaData == null) {
                return list;
            }
            String[] types = {"TABLE"};
            ResultSet tables = metaData.getTables(null, SCHEMA, "%", types);
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                String tableRemarks = tables.getString("REMARKS");
                Table table = new Table(tableName, tableRemarks);
                ResultSet primaryKeys = metaData.getPrimaryKeys(null, SCHEMA, tableName);
                while (primaryKeys.next()) {
                    String primaryKey = primaryKeys.getString("COLUMN_NAME");
                    table.setPrimaryKey(primaryKey);
                }
                // 获取索引
                table.setIndexList(getIndex(metaData, table));
                // 获取列
                table.setColumnList(getColumn(metaData, table));
                list.add(table);
            }
            return list;
        } catch (Exception e) {
            return list;
        }

    }


    // 获取索引信息
    private static List<Index> getIndex(DatabaseMetaData metaData, Table table) {
        String tableName = table.getName();
        List<Index> list = new ArrayList<>();
        if (metaData == null) {
            return list;
        }
        Map<String, List<String>> indexMap = new HashMap<>(16);
        Map<String, Index> map = new HashMap<>(16);
        try {
            ResultSet indexInfo = metaData.getIndexInfo(null, SCHEMA, tableName, false, false);
            while (indexInfo.next()) {
                String indexName = indexInfo.getString("INDEX_NAME");
                String columnName = indexInfo.getString("COLUMN_NAME");
                boolean unique = !indexInfo.getBoolean("NON_UNIQUE");
                short ordinalPosition = indexInfo.getShort("ORDINAL_POSITION");
                if (!map.containsKey(indexName)) {
                    map.put(indexName, new Index().setName(indexName).setUnique(unique));
                }
                // 暂存索引列
                List<String> indexList = indexMap.computeIfAbsent(indexName, k -> new ArrayList<>());
                indexList.add(ordinalPosition - 1, columnName);
            }

            map.forEach((indexName, index) -> {
                List<String> indexList = indexMap.get(indexName);
                index = index.setFields(StringUtils.join(indexList, ","));
                list.add(index);
            });
            return list;
        } catch (Exception e) {
            return list;
        }
    }

    /**
     * 获取表中所有列的信息
     * @param metaData
     * @param table
     * @return
     */
    private static List<Column> getColumn(DatabaseMetaData metaData, Table table) {
        String tableName = table.getName();
        List<Column> list = new ArrayList<>();
        if (metaData == null) {
            return list;
        }
        try {
            ResultSet columns = metaData.getColumns(null, "%", tableName, "%");
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                int dataSize = columns.getInt("COLUMN_SIZE");
                int digits = columns.getInt("DECIMAL_DIGITS");
                boolean nullable = columns.getBoolean("NULLABLE");
                boolean autoIncrement = columns.getBoolean("IS_AUTOINCREMENT");
                String defaultValue = columns.getString("COLUMN_DEF");
                if (defaultValue == null) {
                    defaultValue = "";
                }
                String remarks = columns.getString("REMARKS");
                list.add(new Column(columnName, columnType, dataSize, digits, nullable, autoIncrement, defaultValue, remarks));
            }
            return list;
        } catch (Exception e) {
            return list;
        }
    }

    // 获取数据库连接
    public static Connection getConnection(DbConfig config) throws SQLException, ClassNotFoundException {
        Class.forName(config.getDriver());
        String dbType = getDbType(config.getUrl());
        Properties properties = new Properties();
        properties.setProperty("user", config.getUsername());
        properties.setProperty("password", config.getPassword());
        switch (dbType) {
            case MYSQL:
                // 设置连接属性
                properties.setProperty("remarks", "true");
                properties.setProperty("useInformationSchema", "true");
                break;
            case POSTGRESQL:
                properties.put("remarksReporting","true");//获取数据库的备注信息
                properties.setProperty("useInformationSchema", "true");
                break;
            default:
                properties.setProperty("remarks", "true");
                break;
        }
        Connection connection = DriverManager.getConnection(config.getUrl(), properties);
        log.info(String.valueOf(connection));
        return connection;
    }

    private static String getDbType(String url) {
        if (url.startsWith("jdbc:h2")) {
            return H2;
        } else if (url.startsWith("jdbc:oracle")) {
            return ORACLE;
        } else if (url.startsWith("jdbc:mysql")) {
            return MYSQL;
        } else if (url.startsWith("jdbc:sqlserver")) {
            return SQLSERVER;
        } else if (url.startsWith("jdbc:sap")) {
            return HANA;
        } else if (url.startsWith("jdbc:postgresql")) {
            return POSTGRESQL;
        } else if (url.startsWith("jdbc:zenith")) {
            return ZENITH;
        }
        return "UNKNOWN";
    }


}
