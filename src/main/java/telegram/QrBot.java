package telegram;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import telegram.database.Manager;
import telegram.objects.User;

import java.util.ArrayList;
import java.util.List;
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
            int state = user.getState();

            SendMessage message = new SendMessage()
                    .setChatId(chat_id);

            switch (state) {
                case BotStates.START:
                    message = getMainMenu(user, message);
                    sendCommonMessage(message);
                    break;
                case BotStates.CHANGE_ALIAS:
                    if (update.getMessage().hasText()) {
                        String message_text = update.getMessage().getText();
                        Manager.changeUserAlias(String.valueOf(chat_id), message_text);
                        Manager.changeUserState(String.valueOf(chat_id), BotStates.START);
                        user.setAlias(message_text);
                        sendCommonMessage(message.setText("Имя успешно изменено"));
                        message = getMainMenu(user, message);
                        sendCommonMessage(message);
                    }
                    break;
                case BotStates.UPLOAD_QR_CODE:
                    if (update.getMessage().hasPhoto()) {

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
                                    .doOnSubscribe(id -> Manager.changeUserState(String.valueOf(id), BotStates.CHANGE_ALIAS))
                                    .subscribeOn(Schedulers.newThread())
                                    .timeout(15, TimeUnit.SECONDS)
                                    .subscribe(result -> sendCommonMessage(message.setText("Отправьте новое имя")), e -> sendCommonMessage(message.setText("Ошибка, попробуйте еще раз")));
                            break;
                        case BotCommands.QR_CODE:
                            sendCommonMessage(message.setText("Отправьте QR код"));
                            Manager.changeUserState(String.valueOf(chat_id), BotStates.UPLOAD_QR_CODE);
                            break;
                    }
                    break;
                case (BotStates.CHANGE_ALIAS):
                    Manager.changeUserState(String.valueOf(chat_id), BotStates.START);
                    Manager.changeUserAlias(String.valueOf(chat_id), message.getText());
                    sendCommonMessage(getMainMenu(user, message));
            }


        }
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
