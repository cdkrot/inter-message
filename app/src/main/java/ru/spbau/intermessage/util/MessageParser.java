package ru.spbau.intermessage.util;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ru.spbau.intermessage.core.Chat;
import ru.spbau.intermessage.core.Message;
import ru.spbau.intermessage.core.Messenger;
import ru.spbau.intermessage.core.User;
import ru.spbau.intermessage.gui.Item;
import ru.spbau.intermessage.gui.MessageItem;
import ru.spbau.intermessage.gui.PictureItem;
import ru.spbau.intermessage.gui.SystemItem;

public class MessageParser {
    public static Item[] parseMessages(Messenger messenger, List<Tuple3<User, String, Message>> messages, Chat chat) {
        Item items[] = new Item[messages.size()];

        for (int i = 0; i < messages.size(); i++) {
            items[i] = parseMessage(messenger, messages.get(i).third, chat, messages.get(i).second);
        }

        return items;
    }

    /**
     * Converts bytes in Message to appropriate form in Item.
     * Param chat is used only when it is a system message
     */
    public static Item parseMessage(Messenger messenger, Message message, @Nullable Chat chat, @Nullable User user) {
        return parseMessage(messenger, message, chat, messenger.doGetUserName(user));
    }

    /**
     * Converts bytes in Message to appropriate form in Item.
     * Param chat is used only when it is a system message
     */
    public static Item parseMessage(Messenger messenger, Message message, @Nullable Chat chat, String userName) {
        if ("text".equals(message.type)) {
            String text = Util.bytesToString(message.data);
            return new MessageItem(userName, text, message.timestamp, 0);
        } else if ("picture".equals(message.type)) {
            Bitmap bmp = BitmapHelper.bitmapFromBytes(message.data);
            return new PictureItem(userName, bmp, message.timestamp, 0);
        } else if ("!newname".equals(message.type)) {
            ReadHelper reader = new ReadHelper(ByteVector.wrap(message.data));

            String newName = reader.readString();

            String text = "Dialog was renamed to " + newName + " by " + userName;

            return new SystemItem(text, message.timestamp, 0);
        } else if ("!newchat".equals(message.type)) {
            String chatName = messenger.doGetChatName(chat);
            String text = "The dialog " + chatName +" was created by " + userName;

            return new SystemItem(text, message.timestamp, 0);
        } else if ("!adduser".equals(message.type)) {
            ReadHelper reader = new ReadHelper(ByteVector.wrap(message.data));

            List<String> userNames = new ArrayList<>();
            User user = null;
            while ((user = User.read(reader)) != null) {
                userNames.add(messenger.doGetUserName(user));
            }

            String text;

            if (userNames.size() == 1) {
                text = "User " + userNames.get(0) + " was added to the dialog";
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append("Users ");
                for (int i = 0; i + 1 < userNames.size(); i++) {
                   builder.append(userNames.get(i));
                   builder.append(i + 2 != userNames.size() ? ", " : " and ");
                }
                builder.append(userNames.get(userNames.size() - 1));
                builder.append(" were added to dialog");
                text = builder.toString();
            }

            return new SystemItem(text, message.timestamp, 0);
        } else if ("!leave".equals(message.type)) {
            return new SystemItem(userName + " left the dialog", message.timestamp, 0);
        } else {
            throw new RuntimeException("Unknown type of message");
        }
    }
}
