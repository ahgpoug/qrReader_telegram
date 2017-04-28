package telegram;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import telegram.database.Manager;
import telegram.objects.User;
import telegram.sqlite.SQLiteHelper;
import telegram.util.ZXing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.toIntExact;

public class QrBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && (update.getMessage().hasText() || update.getMessage().hasPhoto())) {
            long chat_id = update.getMessage().getChatId();
            User user = Manager.getUser(String.valueOf(chat_id));

            SendMessage message = new SendMessage()
                    .setChatId(chat_id);

            if (update.getMessage().hasText() && update.getMessage().getText().equals("/start")){
                Observable.defer(() -> Observable.just(chat_id))
                        .doOnNext(id -> Manager.changeUserState(String.valueOf(id), BotStates.START))
                        .doOnNext(id -> user.setState(BotStates.START))
                        .subscribeOn(Schedulers.newThread())
                        .timeout(15, TimeUnit.SECONDS)
                        .subscribe(result -> sendCommonMessage(getMainMenu(user, new SendMessage().setChatId(chat_id))), e -> sendErrorMessage(new SendMessage().setChatId(chat_id)));
            }

            int state = user.getState();

            switch (state){
                case BotStates.START:
                    message = getMainMenu(user, message);
                    sendCommonMessage(message);
                    break;
                case BotStates.CHANGE_ALIAS:
                    if (update.getMessage().hasText()) {
                        String message_text = update.getMessage().getText();
                        Observable.defer(() -> Observable.just(chat_id))
                                .doOnNext(id -> Manager.changeUserAlias(String.valueOf(id), message_text))
                                .doOnNext(id -> user.setAlias(message_text))
                                .doOnNext(id -> Manager.changeUserState(String.valueOf(id), BotStates.START))
                                .map(id -> new SendMessage().setChatId(id))
                                .subscribeOn(Schedulers.newThread())
                                .timeout(15, TimeUnit.SECONDS)
                                .subscribe(result -> {
                                    sendCommonMessage(result.setText("Имя успешно изменено"));
                                    result = getMainMenu(user, result);
                                    sendCommonMessage(result);
                                }, e -> sendErrorMessage(new SendMessage().setChatId(chat_id)));
                    }
                    break;
                case BotStates.UPLOAD_QR_CODE:
                    if (update.getMessage().hasPhoto()) {
                        List<PhotoSize> photos = update.getMessage().getPhoto();

                        Observable.defer(() -> Observable.just(photos.stream().findFirst().orElse(null).))
                                .filter(photo -> photo != null)
                                .map(photo -> getFilePath(photo))
                                .map(path -> downloadPhotoByFilePath(path))
                                .map(file -> ZXing.readQRCode(file))
                                .filter(code -> code != null)
                                .map(code -> SQLiteHelper.checkQrCode(String.valueOf(chat_id), code))
                                .filter(task -> task != null)
                                .subscribeOn(Schedulers.newThread())
                                .timeout(60, TimeUnit.SECONDS)
                                .subscribe(result -> sendCommonMessage(new SendMessage().setChatId(chat_id).setText("Загружено " + result.getTaskName())), e -> sendErrorMessage(new SendMessage().setChatId(chat_id)));
                    }
                    break;
            }
        } else if (update.hasCallbackQuery()) {
            String call_data = update.getCallbackQuery().getData();
            long message_id = update.getCallbackQuery().getMessage().getMessageId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();
            User user = Manager.getUser(String.valueOf(chat_id));
            int state = user.getState();

            EditMessageText editMessage = new EditMessageText()
                    .setChatId(chat_id)
                    .setMessageId(toIntExact(message_id));
            SendMessage message = new SendMessage()
                    .setChatId(chat_id);

            switch (state){
                case (BotStates.START):
                    switch (call_data){
                        case BotCommands.CHANGE_ALIAS:
                            Observable.defer(() -> Observable.just(chat_id))
                                    .doOnNext(id -> Manager.changeUserState(String.valueOf(id), BotStates.CHANGE_ALIAS))
                                    .subscribeOn(Schedulers.newThread())
                                    .timeout(15, TimeUnit.SECONDS)
                                    .subscribe(result -> sendCommonMessage(message.setText("Отправьте новое имя")), e -> sendErrorMessage(message));
                            break;
                        case BotCommands.QR_CODE:
                            Observable.defer(() -> Observable.just(chat_id))
                                    .doOnNext(id -> Manager.changeUserState(String.valueOf(id), BotStates.UPLOAD_QR_CODE))
                                    .subscribeOn(Schedulers.newThread())
                                    .timeout(15, TimeUnit.SECONDS)
                                    .subscribe(result -> sendCommonMessage(message.setText("Отправьте QR код")), e -> sendErrorMessage(message));
                            break;
                    }
                    break;
                case (BotStates.CHANGE_ALIAS):
                    Observable.defer(() -> Observable.just(chat_id))
                            .doOnNext(id -> Manager.changeUserState(String.valueOf(id), BotStates.START))
                            .doOnNext(id -> Manager.changeUserAlias(String.valueOf(id), message.getText()))
                            .subscribeOn(Schedulers.newThread())
                            .timeout(15, TimeUnit.SECONDS)
                            .subscribe(result -> sendCommonMessage(getMainMenu(user, message)), e -> sendErrorMessage(message));
            }
        }
    }

    private void sendErrorMessage(SendMessage message){
        sendCommonMessage(message.setText("Ошибка, попробуйте еще раз"));
    }

    private String getFilePath(PhotoSize photo) {
        Objects.requireNonNull(photo);

        if (photo.hasFilePath()) {
            return photo.getFilePath();
        } else {
            GetFile getFileMethod = new GetFile();
            getFileMethod.setFileId(photo.getFileId());
            try {
                File file = getFile(getFileMethod);
                return file.getFilePath();
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private java.io.File downloadPhotoByFilePath(String filePath) {
        try {
            System.out.println(filePath);
            return downloadFile(filePath);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void sendEditMessage(EditMessageText editMessageText){
        try {
            editMessageText(editMessageText);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendCommonMessage(SendMessage message){
        try {
            sendMessage(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private EditMessageText getMainMenu(User user, EditMessageText editMessageText){
        editMessageText.setText(String.format("Здравствуйте, %s\nГлавное меню.", user.getAlias()));
        editMessageText.setReplyMarkup(getMainMenuKeyboard());
        return editMessageText;
    }

    private SendMessage getMainMenu(User user, SendMessage message){
        message.setText(String.format("Здравствуйте, %s\nГлавное меню.", user.getAlias()));
        message.setReplyMarkup(getMainMenuKeyboard());
        return message;
    }

    private InlineKeyboardMarkup getMainMenuKeyboard(){
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Загрузить QR код").setCallbackData(BotCommands.QR_CODE));
        rowInline.add(new InlineKeyboardButton().setText("Сменить имя").setCallbackData(BotCommands.CHANGE_ALIAS));
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}
