/**
 * @projectName JarPatchTool
 * @package com.jinhy.patch.tool.fx.controller
 * @className com.jinhy.patch.tool.fx.controller.MainController
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.jinhy.patch.tool.fx.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.util.Zip4jUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MainController
 *
 * @author huayu
 * @version TODO
 * @description
 * @date 2024/7/15 20:36
 */
public class MainController {


    @FXML
    private TextField systems;

    @FXML
    private TextField originFolderPath;

    @FXML
    private TextField updateFolderPath;

    @FXML
    private Button executeBtn;

    public void createPatchFiles(MouseEvent mouseEvent) {
        executeBtn.setDisable(true);
        String updateFolder = updateFolderPath.getText();
        String oriFolder = originFolderPath.getText();
        String[] systemArr = StrUtil.splitToArray(StrUtil.trim(systems.getText()), ",");
        long now = new Date().getTime();
        String resultFolder = updateFolder + File.separator + now;
        for (String system : systemArr) {
            String oriBasePath = oriFolder + File.separator + system;
            String updateBasePath = updateFolder + File.separator + system;
            if (!FileUtil.exist(updateBasePath) || !FileUtil.exist(oriBasePath)) {
                continue;
            }

            File needDeleteFile = new File(resultFolder + File.separator + system + ".needDelete.txt");
            List<File> oriFiles = FileUtil.loopFiles(oriBasePath);
            List<File> updateFiles = FileUtil.loopFiles(updateBasePath);

            Set<String> oriFileNameSet = oriFiles.stream()
                    .map(file -> StrUtil.removePrefix(file.getAbsolutePath(), oriFolder + File.separator))
                    .collect(Collectors.toSet());
            Set<String> updateFileNameSet = updateFiles.stream()
                    .map(file -> StrUtil.removePrefix(file.getAbsolutePath(), updateFolder + File.separator))
                    .collect(Collectors.toSet());
            oriFileNameSet.removeAll(updateFileNameSet);
            // 需要删除
            if (CollectionUtil.isNotEmpty(oriFileNameSet)) {
                FileUtil.writeLines(new ArrayList<>(oriFileNameSet), needDeleteFile, "utf-8", true);
            }

            if (FileUtil.isDirectory(oriBasePath)
                    && FileUtil.isDirectory(updateBasePath)) {
                List<File> files = FileUtil.loopFiles(updateBasePath);
                for (File updateFile : files) {
                    // 先判断是否是新增的
                    File oriFile = getOriFile(updateFile);
                    if (!oriFile.exists()) {
                        copyFile(updateFile, resultFolder);
                        continue;
                    }

                    // 如果是文件的话则进行比对
                    if (FileUtil.isFile(updateFile) && !FileUtil.contentEquals(updateFile, oriFile)) {
                        // 如果是jar包则删掉 pom.properties 重新比
                        if (StrUtil.equals(FileUtil.getSuffix(updateFile), "jar")) {
                            deletePomProperties(updateFile);
                            deletePomProperties(oriFile);
                            if (!areJarsEqual(updateFile, oriFile)) {
                                copyFile(updateFile, resultFolder);
                            }
                        } else {
                            copyFile(updateFile, resultFolder);
                        }
                    }
                }
            }
        }

        Alert finishAlert = new Alert(Alert.AlertType.INFORMATION, "增量包生成完成");
        finishAlert.showAndWait();
        executeBtn.setDisable(false);
    }

    private static void deletePomProperties(File updateFile) {
        try (ZipFile zipFile = new ZipFile(updateFile)) {
            List<FileHeader> fileHeaders = zipFile.getFileHeaders();
            FileHeader header = null;
            for (FileHeader fileHeader : fileHeaders) {
                String fileName = fileHeader.getFileName();
                if (StrUtil.containsAll(fileName, "pom.properties", "META-INF")) {
                    header = fileHeader;
                    break;
                }
            }
            if (header != null) {
                zipFile.removeFile(header);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        File file1 = new File("D:\\CompanyWorkspace\\JarPatchTool\\target\\0803.jar");
        File file2 = new File("D:\\CompanyWorkspace\\JarPatchTool\\target\\0805.jar");
        System.out.println(areJarsEqual(file1, file2));
    }

    public static boolean areJarsEqual(File file1, File file2) {
        try (ZipFile zip1 = new ZipFile(file1); ZipFile zip2 = new ZipFile(file2)) {
            List<FileHeader> fhs1 = zip1.getFileHeaders();
            List<FileHeader> fhs2 = zip2.getFileHeaders();
            List<Long> crcList1 = fhs1.stream().map(FileHeader::getCrc).sorted().collect(Collectors.toList());
            List<Long> crcList2 = fhs2.stream().map(FileHeader::getCrc).sorted().collect(Collectors.toList());
            return crcList1.equals(crcList2);
        } catch (IOException e) {
            return false;
        }
    }


    private void copyFile(File updateFile, String resultFolder) {
        String filePath = StrUtil.removePrefix(updateFile.getAbsolutePath(), updateFolderPath.getText() + File.separator);
        String updateFileName = updateFile.getName();
        // 添加一些筛选，筛选其实不该放在这个代码里，应该前置，懒得搞了
        if (StrUtil.contains(filePath, "META-INF") || StrUtil.equals(updateFileName, "git.properties")) {
            return;
        }
        filePath = StrUtil.removeSuffix(filePath, updateFileName);
        FileUtil.copy(updateFile, FileUtil.mkdir(resultFolder + File.separator + filePath), true);
    }

    public File getOriFile(File file) {
        String oriFilePath = StrUtil.replace(file.getAbsolutePath(), updateFolderPath.getText(), originFolderPath.getText());
        return new File(oriFilePath);
    }

    public File getResultFile(File file, String resultFolder) {
        String resultFilePath = StrUtil.replace(file.getAbsolutePath(), updateFolderPath.getText(), resultFolder);
        return new File(resultFilePath);
    }
}