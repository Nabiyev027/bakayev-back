package org.example.backend.services.CommentService;

import org.example.backend.dto.CommentDto;
import org.example.backend.dtoResponse.CommentResDto;

import java.util.List;
import java.util.UUID;

public interface CommentService {
    void addComment(CommentDto commentDto);

    void allowComment(UUID id);

    List<CommentResDto> getComments();

    void deleteComment(UUID id);
}
