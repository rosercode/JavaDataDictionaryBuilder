package cool.wangshuo.ddg.utils;


import cool.wangshuo.ddg.model.Column;
import cool.wangshuo.ddg.MainApp;
import cool.wangshuo.ddg.model.Table;
import fun.mingshan.markdown4j.Markdown;
import fun.mingshan.markdown4j.constant.FlagConstants;
import fun.mingshan.markdown4j.type.block.TableBlock;
import fun.mingshan.markdown4j.type.block.TitleBlock;
import javafx.scene.control.ProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * @author wangshuo
 * @description
 * @createDate 2022/11/10 10:00
 */
@Slf4j
public class CommonUtils {

    // 初始化日志框架
    public static void initLog() {
        InputStream fileInputStream = null;
        try {
            Properties properties = new Properties();
            fileInputStream = MainApp.class.getClassLoader().getResource("log4j.properties").openStream();
            properties.load(fileInputStream);
            PropertyConfigurator.configure(properties);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 转成 md
    public static Markdown convertMd(List<Table> tableList){
        Markdown.MarkdownBuilder mdBuilder = null;
        List<String> titleList = Arrays.asList("字段名称", "字段类型", "字段长度", "可否为空", "是否自增", "默认值", "备注");
        List<TableBlock> tableBlocks = new ArrayList<>();
        List<TitleBlock> titleBlocks = new ArrayList<>();
        log.info("开始遍历导出表，表数目 :" + tableList.size());
        for (Table table : tableList) {
            log.info("开始导出表： " + table.getName());
            TitleBlock secondLevelTitleParamDesc = TitleBlock.builder().level(TitleBlock.Level.SECOND).content(table.getName() + "\n").build();
            titleBlocks.add(secondLevelTitleParamDesc);

            TableBlock.TableBlockBuilder tableBlockBuilder = null;
            ArrayList<TableBlock.TableRow> rows = new ArrayList<>();
            for (Column column : table.getColumnList()) {
                //表格中的行
                log.info("开始导出列：----- " + column.getName());
                TableBlock.TableRow tableRow = new TableBlock.TableRow();
                tableRow.setRows(Arrays.asList(column.getName(), column.getType(), String.valueOf(column.getSize()), String.valueOf(column.isNullable()),
                        String.valueOf(column.isAutoIncrement()), column.getDefaultValue(), column.getRemarks()));
                rows.add(tableRow);
                //表格标题
                tableBlockBuilder = TableBlock.builder()
                        .titles(titleList)
                        .rows(rows);
            }
            tableBlocks.add(tableBlockBuilder.build());
        }
        mdBuilder = Markdown.builder();
        for (int i = 0; i < tableBlocks.size(); i++) {
            mdBuilder
                    .block(titleBlocks.get(i))
                    .block(tableBlocks.get(i));
        }
       return mdBuilder.build();
    }

    // 导出为 html
    public static String exportAsHtml(String md){
//        MutableDataSet options = new MutableDataSet();
//
//        Parser parser = Parser.builder(options).build();
//        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
//
//        Node document = parser.parse(md);
//        String html = renderer.render(document);
//        return html;
        return null;
    }

    // 导出为 docx
    public static void exportAsDocx(){

    }

    // 导出为 pdf
    public static void exportAsPdf(){

    }

    // 导出为 md
    public static void exportAsMd(List<Table> tableList, String filePath){
        try {
            Markdown markdown = convertMd(tableList);
            try (FileOutputStream fos = new FileOutputStream(new File(filePath));
                 OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                 BufferedWriter bw = new BufferedWriter(osw)) {
                String[] arrs = markdown.toString().split(FlagConstants.LINE_BREAK);

                for (String arr : arrs) {
                    bw.write(arr + "\r\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 获取驱动版本的列表
    public static List<String> driverVersionList(String url){
        Document document = null;
        try {
            document = Jsoup.connect(url)
                    .timeout(10000)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
                    .get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String> versionList = document.select("#contents").get(0).select("a").stream()
                .filter(new Predicate<Element>() {
                    @Override
                    public boolean test(Element element) {
                        String href = element.attr("href");
                        if (!(href.equals("../") || href.indexOf('/') == -1)) {
                            return true;
                        }
                        return false;
                    }
                })
                .map(element -> element.attr("href").split("/")[0]).collect(Collectors.toList());
        return versionList;
    }


    /**
     * 下载文件
     * @param url
     * @param file
     * @param progressBar 进度条
     */
    public static void download(URL url, String file, ProgressBar progressBar) {
        long startTime=System.currentTimeMillis();
        BufferedInputStream bis = null;
        Integer bufferSize = 1024;
        FileOutputStream fis = null;
        try {
            fis = new FileOutputStream(file);
            URLConnection conn = url.openConnection();
            int size = conn.getContentLength();
            log.info("远程文件大小为：" + size + " bytes");
            bis = new BufferedInputStream(conn.getInputStream());
            byte[] buffer = new byte[bufferSize];
            int count = 0;
            int counter = 0;
            while ((count = bis.read(buffer, 0, bufferSize)) != -1) {
                fis.write(buffer, 0, count);
                counter++;
                if (progressBar!=null){
                    progressBar.setProgress(bufferSize*new Double(counter)/size);
                }
            }
            fis.close();
            bis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        long endTime=System.currentTimeMillis();
        log.info("下载耗时：" + (endTime-startTime)/1000.0 + " s");
    }

    /**
     * 下载文件到本地
     * @param url 网络资源
     * @param file
     */
    public static void download(URL url, String file) {
        download(url, file, null);
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
