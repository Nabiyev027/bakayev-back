package org.example.backend.services.messageService;

import org.example.backend.entity.MessageText;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    void addNewMessageDesription(String description);

    void deleteMessageText(UUID id);

    List<MessageText> getMessageTexts();

}
