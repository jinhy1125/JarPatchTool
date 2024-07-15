package com.jinhy.patch.tool;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main2 {

    public static final String[] SUB_FOLDER_ARRAY = new String[]{"clzx-dzjz-bp", "dossier"};

    public static final String ORI_FOLDER = "D:\\CompanyWorkspace\\卷宗\\工作记录\\打包\\20240712\\update001";

    public static final String UPDATE_FOLDER = "D:\\CompanyWorkspace\\卷宗\\工作记录\\打包\\20240715\\update001";

    public static void main(String[] args) {
        long now = new Date().getTime();
        String resultFolder = UPDATE_FOLDER + File.separator + now;
        for (String system : SUB_FOLDER_ARRAY) {
            String oriBasePath = ORI_FOLDER + File.separator + system;
            String updateBasePath = UPDATE_FOLDER + File.separator + system;

            File needDeleteFile = new File(resultFolder + File.separator + system + ".needDelete.txt");
            List<File> oriFiles = FileUtil.loopFiles(oriBasePath);
            List<File> updateFiles = FileUtil.loopFiles(updateBasePath);

            Set<String> oriFileNameSet = oriFiles.stream()
                    .map(file -> StrUtil.removePrefix(file.getAbsolutePath(), ORI_FOLDER + File.separator))
                    .collect(Collectors.toSet());
            Set<String> updateFileNameSet = updateFiles.stream()
                    .map(file -> StrUtil.removePrefix(file.getAbsolutePath(), UPDATE_FOLDER + File.separator))
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
    }

    private static void copyFile(File updateFile, String resultFolder) {
        String filePath = StrUtil.removePrefix(updateFile.getAbsolutePath(), UPDATE_FOLDER + File.separator);
        filePath = StrUtil.removeSuffix(filePath, updateFile.getName());
        FileUtil.copy(updateFile, FileUtil.mkdir(resultFolder + File.separator + filePath), true);
    }

    public static File getOriFile(File file) {
        String oriFilePath = StrUtil.replace(file.getAbsolutePath(), UPDATE_FOLDER, ORI_FOLDER);
        return new File(oriFilePath);
    }

    public static File getResultFile(File file, String resultFolder) {
        String resultFilePath = StrUtil.replace(file.getAbsolutePath(), UPDATE_FOLDER, resultFolder);
        return new File(resultFilePath);
    }
}