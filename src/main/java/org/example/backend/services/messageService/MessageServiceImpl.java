package org.example.backend.services.messageService;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.MessageText;
import org.example.backend.repository.MessageTextRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageTextRepo messageTextRepo;

    @Override
    public void addNewMessageDesription(String description) {
        MessageText messageText = new MessageText();
        messageText.setDescription(description);
        messageTextRepo.save(messageText);
    }

    @Override
    public void deleteMessageText(UUID id) {
        if (!messageTextRepo.existsById(id)) {
            throw new RuntimeException("Message text not found with id: " + id);
        }
        messageTextRepo.deleteById(id);
    }

    @Override
    public List<MessageText> getMessageTexts() {
        return messageTextRepo.findAll();
    }

}
