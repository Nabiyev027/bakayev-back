package org.example.backend.services.CommentService;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.CommentDto;
import org.example.backend.dtoResponse.CommentResDto;
import org.example.backend.entity.Comment;
import org.example.backend.repository.CommentRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepo commentRepo;

    @Override
    public void addComment(CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setFirstName(commentDto.getFirstName());
        comment.setLastName(commentDto.getLastName());
        comment.setText(commentDto.getText());
        comment.setRate(commentDto.getRate());
        comment.setStatus(false);
        comment.setDate(LocalDate.now());

        commentRepo.save(comment);
    }

    @Override
    public void allowComment(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Comment ID cannot be null");
        }

        Comment comment = commentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        if(comment.getStatus().equals(false)) {
            comment.setStatus(true);
        }else {
            comment.setStatus(false);
        }

        commentRepo.save(comment);
    }

    @Override
    public List<CommentResDto> getComments() {
        List<CommentResDto> list = new ArrayList<>();

        List<Comment> all = commentRepo.findAll();
        for (Comment comment : all) {
            CommentResDto dto = new CommentResDto();
            dto.setId(comment.getId());
            dto.setFirstName(comment.getFirstName());
            dto.setLastName(comment.getLastName());
            dto.setText(comment.getText());
            dto.setRate(comment.getRate());
            dto.setStatus(comment.getStatus());
            list.add(dto);
        }

        return  list;
    }

    @Override
    public void deleteComment(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Comment ID cannot be null");
        }

        commentRepo.deleteById(id);
    }


}
