package ru.practicum.shareit.item.mappers;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@UtilityClass
public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .authorName(comment.getAuthor().getName())
                .build();
    }

    public static Comment toComment(CommentDto commentDto, Item item, User user, LocalDateTime dateTime) {
        return Comment.builder()
                .text(commentDto.getText())
                .created(dateTime)
                .item(item)
                .author(user)
                .build();
    }

    public static List<CommentDto> toCommentDtoList(Iterable<Comment> comments) {
        return StreamSupport.stream(comments.spliterator(), false)
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}