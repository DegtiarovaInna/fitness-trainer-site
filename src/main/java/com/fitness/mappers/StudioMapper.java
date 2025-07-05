package com.fitness.mappers;

import com.fitness.dto.StudioDTO;
import com.fitness.models.Studio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface StudioMapper {


    // Маппинг из Studio в StudioDTO
    @Mapping(target = "adminId", expression = "java(studio.getAdmin() != null ? studio.getAdmin().getId() : null)")
    StudioDTO studioToStudioDTO(Studio studio);

    // Маппинг из StudioDTO в Studio в сервисе Поскольку мы создаём/обновляем студию через отдельный
    // DTO без adminId, а назначение админа происходит спец-методом, обратный маппинг из DTO в Entity нам не нужен

   // Studio studioDTOToStudio(StudioDTO studioDTO);
}
