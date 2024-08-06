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

import java.io.File;
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
                        copyFile(updateFile, resultFolder);
                        continue;
                    }
                }
            }
        }

        Alert finishAlert = new Alert(Alert.AlertType.INFORMATION, "增量包生成完成");
        finishAlert.showAndWait();
        executeBtn.setDisable(false);
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