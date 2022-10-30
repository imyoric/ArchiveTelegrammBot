package org.example;

import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.send.SendVideo;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.*;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class Bot extends TelegramLongPollingBot {
    public static String[] chats = new String[1]; //Кол-во резервных чатов
    public static String pa = "archive/";//Путь до архива
    public static boolean ae = true; //Сохранять ли в резервную копию файлы?
    //Из-за ограничений API телеграмма файлы могут скачиватся до 20мб + фотографии скачиваются в плохом качестве
    //Так-что все файлы желательно отправлять без сжатия и в виде документов, но это ограничение касается только
    //Сохранения файлов, поэтому пересылаются все файлы в другие чаты без ограничений за счет отправи не самого файла
    //А его ID
    @Override
    public void onUpdateReceived(Update update) {
        chats[0] = "";//ИД резервных чатов(Отсчет с нуля, 0 = 1)
        for (int a = 0; a < chats.length; a++) {
            try {
                if (update.getChannelPost().getChatId() == null) {
                    return;
                }
            } catch (NullPointerException e) {
                return;
            }
            if (!chats[a].equalsIgnoreCase(String.valueOf(update.getChannelPost().getChatId()))) {
                String chatId = String.valueOf(chats[a]);
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                String text = update.getChannelPost().getText();
                if (update.getChannelPost().getPhoto() != null) { //Обработка фото
                    SendPhoto doc = new SendPhoto();
                    doc.setChatId(chatId);
                    doc.setPhoto(update.getChannelPost().getPhoto().get(0).getFileId());
                    try {
                        sendPhoto(doc);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    if(ae) getPhoto(update); //Сохранение в архив
                }
                if (update.getChannelPost().getVideo() != null) {//Обработка видео
                    SendVideo doc = new SendVideo();
                    doc.setChatId(chatId);
                    doc.setVideo(update.getChannelPost().getVideo().getFileId());
                    try {
                        sendVideo(doc);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    if(ae) getVideo(update); //Сохранение в архив
                }
                if (update.getChannelPost().getDocument() != null) {//Обработка Документов (GIF, PDF итд...)
                    SendDocument doc = new SendDocument();
                    doc.setChatId(chatId);
                    doc.setDocument(update.getChannelPost().getDocument().getFileId());
                    try {
                        sendDocument(doc);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    if(ae) getDoc(update); //Сохранение в архив
                }
                if (update.getChannelPost().getText() != null) {//Обработка сообщений в.т.ч ссылок
                    message.setText(text);
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        }
    }

    @Override
    public String getBotUsername() {//Имя бота
        return "Testbot";
    }

    @Override
    public String getBotToken() {//токен Бота
        return "";
    }
//Далее функции для сохранения обьектов в архив
    public Boolean getPhoto(Update update) {
        String uploadedFileId = update.getChannelPost().getPhoto().get(0).getFileId();
        GetFile uploadedFile = new GetFile();
        uploadedFile.setFileId(uploadedFileId);
        String uploadedFilePath = null;
        try {
            uploadedFilePath = getFile(uploadedFile).getFilePath();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        java.io.File localFile = new java.io.File(pa+uploadedFilePath);
        InputStream is = null;
        try {
            is = new URL("https://api.telegram.org/file/bot" + getBotToken() + "/" + uploadedFilePath).openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            FileUtils.copyInputStreamToFile(is, localFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public Boolean getVideo(Update update) {
        String uploadedFileId = update.getChannelPost().getVideo().getFileId();
        GetFile uploadedFile = new GetFile();
        uploadedFile.setFileId(uploadedFileId);
        String uploadedFilePath = null;
        try {
            uploadedFilePath = getFile(uploadedFile).getFilePath();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        java.io.File localFile = new java.io.File(pa+uploadedFilePath);
        InputStream is = null;
        try {
            is = new URL("https://api.telegram.org/file/bot" + getBotToken() + "/" + uploadedFilePath).openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            FileUtils.copyInputStreamToFile(is, localFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
    public Boolean getDoc(Update update) {
        String uploadedFileId = update.getChannelPost().getDocument().getFileId();
        GetFile uploadedFile = new GetFile();
        uploadedFile.setFileId(uploadedFileId);
        String uploadedFilePath = null;
        try {
            uploadedFilePath = getFile(uploadedFile).getFilePath();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        java.io.File localFile = new java.io.File(pa+uploadedFilePath);
        InputStream is = null;
        try {
            is = new URL("https://api.telegram.org/file/bot" + getBotToken() + "/" + uploadedFilePath).openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            FileUtils.copyInputStreamToFile(is, localFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
