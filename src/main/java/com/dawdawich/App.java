package com.dawdawich;

import com.dawdawich.bot.Bot;
import com.dawdawich.configs.Configuration;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class App {

    public static void main(String[] args) throws IOException, TelegramApiRequestException {
        ApiContextInitializer.init();

        File[] files = new File("./postRedirecterConf").listFiles();
        ArrayList<Configuration> configs = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".properties")) {
                    configs.add(new Configuration(file));
                }
            }
        }

        for (Configuration conf : configs) {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
            Bot bot = new Bot(conf);
            telegramBotsApi.registerBot(bot);
            Thread threadBot = new Thread(bot);
            threadBot.setDaemon(true);
            threadBot.start();
        }
    }

}
