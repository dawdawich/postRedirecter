package com.dawdawich.bot;

import com.dawdawich.configs.Configuration;
import org.telegram.telegrambots.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.media.InputMedia;
import org.telegram.telegrambots.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Bot extends TelegramLongPollingBot implements Runnable {

    private static HashMap<String, List<String>> mediaGroup = new HashMap<>();
    private static Configuration conf;

    public Bot(Configuration conf) {
        this.conf = conf;
    }

    public static SendMediaGroup crateMediaGroup(File file, Long chatId) {
        return crateMediaGroup(file, chatId, null);
    }

    public static SendMediaGroup crateMediaGroup(File file, Long chatId, String caption) {
        File[] photos;
        if (file.listFiles() != null) {
            photos = Arrays
                    .stream(file.listFiles())
                    .sorted((f1, f2) -> {
                        String name1 = f1.getName();
                        String name2 = f2.getName();
                        name1 = name1.substring(0, name1.indexOf('.'));
                        name2 = name2.substring(0, name2.indexOf('.'));
                        int index1 = Integer.parseInt(name1);
                        int index2 = Integer.parseInt(name2);
                        return Integer.compare(index1, index2);
                    })
                    .toArray(File[]::new);
        } else {
            photos = new File[0];
        }
        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setMedia(setInputMedia(photos));
        sendMediaGroup.setChatId(chatId);
        if (caption != null && !caption.isEmpty()) {
            sendMediaGroup.getMedia().get(0).setCaption(caption);
        }
        return sendMediaGroup;
    }

    public static SendMediaGroup crateMediaGroup(List<String> ids, Long chatId, String caption) {
        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setMedia(setInputMedia(ids));
        sendMediaGroup.setChatId(chatId);
        if (caption != null && !caption.isEmpty()) {
            sendMediaGroup.getMedia().get(0).setCaption(caption);
        }
        return sendMediaGroup;
    }

    public static List<InputMedia> setInputMedia(File[] photos) {
        List<InputMedia> photoList = new ArrayList<>();
        Arrays.stream(photos).forEach(f -> {
            InputMediaPhoto inputMediaPhoto = new InputMediaPhoto();
            inputMediaPhoto.setMedia(f, f.getAbsolutePath());
            photoList.add(inputMediaPhoto);
        });
        return photoList;
    }

    public static List<InputMedia> setInputMedia(List<String> ids) {
        List<InputMedia> photoList = new ArrayList<>();
        ids.forEach(s -> {
            InputMediaPhoto inputMediaPhoto = new InputMediaPhoto();
            inputMediaPhoto.setMedia(s);
            photoList.add(inputMediaPhoto);
        });
        return photoList;
    }

    public static SendPhoto createSendPhoto(File image, Long chatId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setNewPhoto(image);
        sendPhoto.setChatId(chatId);
        return sendPhoto;
    }

    public static SendPhoto createSendPhoto(File image, Long chatId, String caption) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setNewPhoto(image);
        sendPhoto.setChatId(chatId);
        sendPhoto.setCaption(caption);
        return sendPhoto;
    }

    public static String getAttachText(File file) {
        String textName;
        if (file.isDirectory()) {
            textName = file.getAbsolutePath().concat(".txt");
        } else {
            textName = file.getAbsolutePath().replace("jpg", "txt");
        }
        if (Files.exists(Paths.get(textName))) {
            try {
                return Files.lines(Paths.get(textName)).reduce("", String::concat);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void sendPosts() {
        File memeFolder = new File(conf.getPath() + File.separator);
        File[] memes = memeFolder.listFiles();
        if (memes != null) {
            Arrays.stream(memes)
                    .filter(f -> !f.getName().contains("txt"))
                    .forEach(file -> {
                        String attachedText = getAttachText(file);
                        if (file.isDirectory()) {
                            List<Message> messages;
                            try {
                                if (attachedText != null && !attachedText.isEmpty()) {
                                    messages = sendMediaGroup(crateMediaGroup(file, conf.getChatId(), attachedText));
                                } else {
                                    messages = sendMediaGroup(crateMediaGroup(file, conf.getChatId()));
                                }
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                                return;
                            }
                            mediaGroup.put(file.getName(), messages.stream().map(m -> m.getPhoto().get(0).getFileId()
                            ).collect(Collectors.toList()));
                            SendMessage markup = new SendMessage();
                            markup.setChatId(conf.getChatId());
                            markup.setText("↑");
                            try {
                                execute(markup);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                        try {
                            if (attachedText != null && !attachedText.isEmpty()) {
                                sendPhoto(createSendPhoto(file, conf.getChatId(), attachedText));
                            } else {
                                sendPhoto(createSendPhoto(file, conf.getChatId()));
                            }
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println();
    }

    @Override
    public String getBotUsername() {
        return "";
    }

    @Override
    public String getBotToken() {
        return conf.getBotId();
    }

    @Override
    public void run() {
        while (true) {
            sendPosts();
        }
    }
}
