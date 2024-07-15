package com.jinhy.patch.tool;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

    public static final String[] SUB_FOLDER_ARRAY = new String[]{"clzx-dzjz-bp", "dossier"};

    public static final String ORI_FOLDER = "D:\\CompanyWorkspace\\卷宗\\工作记录\\打包\\原始版本文件";

    public static final String UPDATE_FOLDER = "D:\\CompanyWorkspace\\卷宗\\工作记录\\打包\\20240712\\update001";

    public static void main(String[] args) {
        String now = DateUtil.now();
        String resultFolder = UPDATE_FOLDER + File.separator + now;
        for (String system : SUB_FOLDER_ARRAY) {
            String oriBasePath = ORI_FOLDER + File.separator + system;
            String updateBasePath = UPDATE_FOLDER + File.separator + system;
            File needDeleteFile = new File(resultFolder + File.separator + system + ".needDelete.txt");

            if (FileUtil.isDirectory(oriBasePath)
                    && FileUtil.isDirectory(updateBasePath)) {
                List<File> files = FileUtil.loopFiles(updateBasePath);
                checkFiles(system, files, resultFolder, needDeleteFile);
            }
        }
    }

    private static void checkFiles(String system, List<File> files, String resultFolder, File needDeleteFile) {
        for (File updateFile : files) {
            // 先判断是否是新增的
            File oriFile = getOriFile(updateFile);
            if (!oriFile.exists()) {
                FileUtil.copy(updateFile, new File(resultFolder + system), true);
                continue;
            }

            // 如果是文件的话则进行比对
            if (FileUtil.isFile(updateFile) && !FileUtil.contentEquals(updateFile, oriFile)) {
                FileUtil.copy(updateFile, new File(resultFolder + system), true);
                continue;
            }

            // 说明是目录，先要比对文件，是否需要删除
            List<File> oriFiles = FileUtil.loopFiles(oriFile);
            List<File> updateFiles = FileUtil.loopFiles(updateFile);

            Set<String> oriFileNameSet = oriFiles.stream()
                    .map(file -> StrUtil.removePrefix(file.getAbsolutePath(), ORI_FOLDER))
                    .collect(Collectors.toSet());
            Set<String> updateFileNameSet = updateFiles.stream()
                    .map(file -> StrUtil.removePrefix(file.getAbsolutePath(), UPDATE_FOLDER))
                    .collect(Collectors.toSet());
            oriFileNameSet.removeAll(updateFileNameSet);
            // 需要删除
            if (CollectionUtil.isNotEmpty(oriFileNameSet)) {
                FileUtil.writeLines(new ArrayList<>(oriFileNameSet), needDeleteFile, "utf-8", true);
            }
            checkFiles(system, files, resultFolder, needDeleteFile);

        }
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