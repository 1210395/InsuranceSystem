package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Entity.Conversation;
import com.insurancesystem.Model.Entity.Message;
import com.insurancesystem.Model.Dto.ConversationDTO;
import com.insurancesystem.Model.Dto.MessageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    ChatMapper INSTANCE = Mappers.getMapper(ChatMapper.class);

    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "receiver.id", target = "receiverId")
    @Mapping(source = "sentAt", target = "sentAt", qualifiedByName = "instantToLocalDateTime")
    MessageDTO toMessageDTO(Message message);

    @Mapping(target = "messages", expression = "java(conversation.getMessages() == null ? null : conversation.getMessages().stream().map(this::toMessageDTO).toList())")
    ConversationDTO toConversationDTO(Conversation conversation);

    @Named("instantToLocalDateTime")
    static LocalDateTime instantToLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
