package net.zulkar.jb.core;

import com.typesafe.config.ConfigFactory;
import net.zulkar.jb.core.handlers.zip.ZipHandler;
import net.zulkar.jb.core.local.LocalStorage;
import net.zulkar.jb.core.ui.ActionManager;
import net.zulkar.jb.core.ui.MainFrame;
import net.zulkar.jb.core.ui.action.OpenAction;
import net.zulkar.jb.core.ui.action.SwitchAction;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class FileManagerMain extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LocalStorage left = new LocalStorage(new ZipHandler());
                LocalStorage right = new LocalStorage(new ZipHandler());
                try {
                    MainFrame frame = new MainFrame();
                    ActionManager actionManager = new ActionManager(ConfigFactory.load("application.conf").getConfig("keymap"));

                    actionManager.init(frame,
                            new OpenAction.Factory(),
                            new SwitchAction.Factory()
                    );
                    frame.init(left, right, actionManager);


                    frame.pack();
                    //frame.setLocationByPlatform(true);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

}
