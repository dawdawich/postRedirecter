package com.dawdawich.configs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

    private String path;
    private String botId;
    private long chatId;

    public Configuration(File props) throws IOException {
        Properties properties = new Properties();
        try (InputStream is = new FileInputStream(props)) {
            properties.load(is);
        }
        this.path = properties.getProperty("path");
        botId = properties.getProperty("botId");
        chatId = Long.parseLong(properties.getProperty("chatId"));
    }

    public String getBotId() {
        return botId;
    }

    public String getPath() {
        return path;
    }

    public long getChatId() {
        return chatId;
    }

}
