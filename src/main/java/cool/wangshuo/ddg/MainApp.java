package cool.wangshuo.ddg;

import cool.wangshuo.ddg.view.OverviewController;
import cool.wangshuo.ddg.view.RootLayoutController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static cool.wangshuo.ddg.utils.CommonUtils.initLog;


/**
 * @author wangshuo
 * @description
 * @createDate 2022/11/10 10:00
 */

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    // 项目配置保存目录
    public static final String configPath = System.getProperty("user.home") + File.separator + ".ddg";

    // cache 保存缓存的驱动程序，<数据库名称>/版本
    // cache.json 缓存信息 比如: 驱动程序版本列表

    public static final Map<String, Map> dbMap = new HashMap();

    static {
        // mysql 相关的信息
        Map<String, String> mysqlMap = new HashMap();
        // 1、驱动下载连接 【源】
        mysqlMap.put("baseUrl", "https://repo1.maven.org/maven2/mysql/mysql-connector-java/");
        // 2、驱动类路径
        mysqlMap.put("driverClassPath", "com.mysql.cj.jdbc.Driver");
        // 3、默认的服务端口号
        mysqlMap.put("defaultPort", "3307");
        // 4、默认的用户名
        mysqlMap.put("defaultUsername", "root");
        // 5、jar 包名称
        mysqlMap.put("driverName", "mysql-connector-java");
        // 6、推荐使用的驱动版本
        mysqlMap.put("suggestVersion", "8.0.30");
        // 7、默认的连接参数
        mysqlMap.put("arguments", "useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&nullCatalogMeansCurrent=true");

        Map<String, String> postgresMap = new HashMap();
        postgresMap.put("baseUrl", "https://repo1.maven.org/maven2/org/postgresql/postgresql/");
        postgresMap.put("driverClassPath", "org.postgresql.Driver");
        postgresMap.put("driverName", "postgresql");
        postgresMap.put("suggestVersion", "42.2.6");
        postgresMap.put("arguments", "characterEncoding=UTF-8");
        postgresMap.put("defaultPort", "5432");
        postgresMap.put("defaultUsername", "postgres");

        Map<String, String> sqlServerMap = new HashMap();

        dbMap.put("mysql", mysqlMap);
        // dbMap.put("postgresql", postgresMap);

        File configFile = new File(configPath);
        if (configFile.isDirectory() && !configFile.exists()) {
            configFile.mkdirs();
        }

    }

    /**
     * Constructor
     */
    public MainApp() {

    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("数据字典生成器 （data dictionary generator）");
        // Set the application icon.
        this.primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource("icon/logo.png").toString()));
        initRootLayout();
        showOverview();
    }

    /**
     * Initializes the root layout
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            // Give the controller access to the main app.
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the overview inside the root layout.
     */
    public void showOverview() {
        try {
            // Load  overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("view/Overview.fxml"));
            AnchorPane overview = (AnchorPane) loader.load();
            rootLayout.setCenter(overview);
            OverviewController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    public static void main(String[] args) {
        initLog();
        launch(args);
    }

}