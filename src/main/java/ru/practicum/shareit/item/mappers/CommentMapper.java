package ru.practicum.shareit.item.mappers;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Singleton
@Mapper(componentModel = "spring")
public class CommentMapper {

    private static CommentMapper instance = new CommentMapper();

    CommentMapper() {
    }

    public static CommentMapper getInstance() {
        if (instance == null) {
            instance = new CommentMapper();
        }
        return instance;
    }

    public CommentDto returnCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .authorName(comment.getAuthor().getName())
                .build();
    }

    public Comment returnComment(CommentDto commentDto, Item item, User user, LocalDateTime dateTime) {
        return Comment.builder()
                .text(commentDto.getText())
                .created(dateTime)
                .item(item)
                .author(user)
                .build();
    }

    public List<CommentDto> returnICommentDtoList(Iterable<Comment> comments) {
        List<CommentDto> result = new ArrayList<>();

        for (Comment comment : comments) {
            result.add(returnCommentDto(comment));
        }
        return result;
    }
}
