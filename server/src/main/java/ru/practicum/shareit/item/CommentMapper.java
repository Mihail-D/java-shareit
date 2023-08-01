package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto commentDto = CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .authorName(comment.getAuthor().getName())
                .build();
        return commentDto;
    }

    public static Comment toComment(CommentDto commentDto, Item item, User user, LocalDateTime dateTime) {
       Comment comment = Comment.builder()
                .text(commentDto.getText())
                .created(dateTime)
                .item(item)
                .author(user)
                .build();
        return comment;
    }

    public static List<CommentDto> toCommentDtoList(Iterable<Comment> comments) {
        List<CommentDto> result = new ArrayList<>();

        for (Comment comment : comments) {
            result.add(toCommentDto(comment));
        }
        return result;
    }
}
