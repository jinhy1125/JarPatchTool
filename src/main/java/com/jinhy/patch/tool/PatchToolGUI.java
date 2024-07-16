/**
 * @projectName JarPatchTool
 * @package com.jinhy.patch.tool
 * @className com.jinhy.patch.tool.PatchToolGUI
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.jinhy.patch.tool;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * PatchToolGUI
 * @description
 * @author huayu
 * @date 2024/7/15 20:20
 * @version TODO
 */
public class PatchToolGUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/patch-tool.fxml"));
        Parent root = loader.load();

        // 设置场景
        Scene scene = new Scene(root);

        // 设置舞台（窗口）的标题
        primaryStage.setTitle("JavaFX Client Program");

        // 设置舞台的场景
        primaryStage.setScene(scene);

        // 显示舞台
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}