package cool.wangshuo.ddg.view;

import cool.wangshuo.ddg.*;
import cool.wangshuo.ddg.model.DbConfig;
import cool.wangshuo.ddg.model.Table;
import cool.wangshuo.ddg.utils.CommonUtils;
import cool.wangshuo.ddg.utils.DbUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static cool.wangshuo.ddg.utils.DbUtils.*;

@Slf4j
public class OverviewController {

    // Reference to the main application.
    private MainApp mainApp;

    // 数据库类型
    @FXML
    private ComboBox<String> dbTypeComboBox;

    // 驱动版本
    @FXML
    private ComboBox<String> driverVersionListComboBox;


    @FXML
    private TextField urlTextField, userTextField,passTextField;

    // 数据库名称
    @FXML
    private ComboBox<String> dbNameComboBox;

    public static final String urlFormat = "jdbc:%s://%s:%d/%s?%s";

    public OverviewController() {

    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        MainApp.dbMap.entrySet().stream().forEach((Map.Entry entry)->{
            dbTypeComboBox.getItems().add((String) entry.getKey());
        });
        dbTypeComboBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                log.info("你选择了 " + dbTypeComboBox.getItems().get((Integer) newValue) + "，开始获取驱动版本列表");
                String value = dbTypeComboBox.getItems().get((Integer) newValue);
                Map<String, String> dbMap = MainApp.dbMap.get(value);
                String suggestVersion = (String) dbMap.get("suggestVersion");
                log.info("驱动推荐版本为 " + suggestVersion);
                driverVersionListComboBox.setValue(suggestVersion);

                log.info("本地缓存不存在数据库版本列表，联网获取版本列表");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        driverVersionListComboBox.getItems().clear();
                        driverVersionListComboBox.getItems().addAll(CommonUtils.driverVersionList(dbMap.get("baseUrl")));
                        log.info("从网络中获取驱动列表成功");
                    }
                }).start();

                urlTextField.setText(String.format(urlFormat, value, "10.0.8.11", Integer.valueOf(dbMap.get("defaultPort")), "", dbMap.get("arguments")));
                userTextField.setText(dbMap.get("defaultUsername"));
            }
        });
        driverVersionListComboBox.setVisibleRowCount(10);
        driverVersionListComboBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                String value = driverVersionListComboBox.getItems().get((Integer) newValue);
                log.info("驱动版本更新 " + value);
            }
        });

        dbNameComboBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                String driverUrl = urlTextField.getText();
                String value = dbNameComboBox.getItems().get((Integer) newValue);
                urlTextField.setText(driverUrl.substring(0,driverUrl.lastIndexOf("/")+1) + value + driverUrl.substring(driverUrl.lastIndexOf("?")));
            }
        });
        // Clear person details.

    }

    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * export as md
     * @param event
     */
    @FXML
    public void exportAsMD(javafx.event.ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));

        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Md files (*.md)", "*.md");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show save file dialog
        File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

        if (file != null) {
            // Make sure it has the correct extension
            if (!file.getPath().endsWith(".md")) {
                file = new File(file.getPath() + ".md");
            }
        }
        List<Table> tableList;
        try {
            tableList = getTables(getConnection().getMetaData());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        CommonUtils.exportAsMd(tableList, file.getAbsolutePath());
        log.info("MD 数据导出完成");
    }
    public void alertErr(){
        Alert alert = new Alert(Alert.AlertType.WARNING); // 创建一个警告对话框
        alert.setHeaderText("运行警告"); // 设置对话框的头部文本
        // 设置对话框的内容文本
        alert.setContentText("数据库链接失败，请检查相关配置信息。");
        alert.show(); // 显示对话框
    }

    // 从界面的数据中生成一个连接
    private Connection getConnection() {
        String dbType = dbTypeComboBox.getSelectionModel().getSelectedItem();
        String driverVersion = driverVersionListComboBox.getSelectionModel().getSelectedItem();
        String url = urlTextField.getText();
        String user = userTextField.getText();
        String passwd = passTextField.getText();

        Map dbMap = MainApp.dbMap.get(dbType);

        File localDriverDir = new File(new StringBuilder()
                .append(MainApp.configPath).append(File.separator)
                .append("cache").append(File.separator)
                .append(dbTypeComboBox.getValue()).toString());
        File localDriverFile = new File(new StringBuilder(localDriverDir.getAbsolutePath()).append(File.separator)
                .append(dbType).append("-").append(driverVersion).append(".jar")
                .toString());
        Connection conn = null;
        log.info("检查本地驱动是否存在 " + localDriverFile.getAbsolutePath());
        if (!localDriverFile.exists()){
            String driverUrl =  dbMap.get("baseUrl") + driverVersion   + "/" + dbMap.get("driverName") + "-" + driverVersion + ".jar";
            log.info("本地缓存驱动不存在，开始联网下载驱动程序，下载地址为 " + driverUrl + ", 下载到 " + localDriverFile.getAbsolutePath());
            if (!localDriverDir.exists()){
                localDriverDir.mkdirs();
            }
            try {
                log.info("开始下载驱动");
                CommonUtils.download(new URL(driverUrl), localDriverFile.getAbsolutePath());
                log.info("结束下载驱动");

            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }else{
            log.info("本地缓存中存储驱动程序，位置 " + localDriverFile.getAbsolutePath());
        }
        log.info("加载本地驱动程序（" + localDriverFile.getAbsolutePath() +"）到JVM中");
        CommonUtils.loadDriver(localDriverFile);
        DbConfig dbConfig = new DbConfig();
        dbConfig.setDriver((String) dbMap.get("driverClassPath"));
        dbConfig.setUrl(url);
        dbConfig.setUsername(user);
        dbConfig.setPassword(passwd);
        try {
            conn = DbUtils.getConnection(dbConfig);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return conn;
    }

    // 测试连接
    @FXML
    public void testConn(ActionEvent actionEvent) {
        log.info("根据选择的配置测试数据库连接");
        Connection conn = null;
        conn = getConnection();

        log.info("数据库连接获取成功");
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // 创建一个消息对话框
        alert.setHeaderText("友情提示"); // 设置对话框的头部文本
        alert.setContentText("数据连接成功。请选择您要导出的数据库。");
        alert.show(); // 显示对话框
        try {
            List<String> dbList = getDBList(conn);
            dbNameComboBox.getItems().clear();
            dbNameComboBox.getItems().addAll(dbList);
            log.info("数据库列表获取成功，数据库数目：" + dbList.size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    // 编辑数据库 url
    public void editUrl(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Pane pane = new Pane();
        TextField alertUrlTextField = new TextField(urlTextField.getText());
        alertUrlTextField.setMinSize(350,20);
        pane.getChildren().add(alertUrlTextField);
        alert.getDialogPane().setExpandableContent(pane);
        Optional<ButtonType> buttonType = alert.showAndWait();
        if (buttonType.isPresent() && buttonType.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE)){
            urlTextField.setText(alertUrlTextField.getText());
        }

    }
}
