package app.burmistrov.congifuration;

import org.hibernate.cfg.MappingSettings;
import org.hibernate.type.format.jackson.Jackson3JsonFormatMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * Заставляет Hibernate сериализовать поля {@code @JdbcTypeCode(SqlTypes.JSON)} через Jackson 3 —
 * тот же, на котором работает остальное приложение.
 *
 * <p>По умолчанию Hibernate берёт Jackson 2 без модуля JSR-310, и любое поле с {@code LocalDate}
 * внутри JSON падает при сохранении. В Jackson 3 поддержка {@code java.time} встроена, поэтому
 * достаточно указать нужный маппер: если Spring уже создал свой, берём его, иначе — по умолчанию.
 */
@Configuration
public class JsonMappingConfig {

    @Bean
    public HibernatePropertiesCustomizer jsonFormatMapperCustomizer(ObjectProvider<JsonMapper> jsonMapper) {
        JsonMapper mapper = jsonMapper.getIfAvailable();
        Jackson3JsonFormatMapper formatMapper =
                mapper != null ? new Jackson3JsonFormatMapper(mapper) : new Jackson3JsonFormatMapper();
        return properties -> properties.put(MappingSettings.JSON_FORMAT_MAPPER, formatMapper);
    }
}
