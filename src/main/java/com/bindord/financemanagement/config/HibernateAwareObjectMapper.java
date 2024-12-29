package com.bindord.financemanagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

/**
 * The type Hibernate aware object mapper.
 * @author pettercarranza
 */
public class HibernateAwareObjectMapper extends ObjectMapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Custom ObjectMapper that adds new Hibernate6Module
     * for serialization/deserialization process to avoid exception
     * <b>JsonMappingException</b>: <i>could not initialize proxy - no Session</i>
     * when you label @ManyToOne or @OneToMany props with FetchType.LAZY.
     * This is used within the method {@link WebMvcConfiguration#jacksonMessageConverter jacksonMessageConverter}
     */
    public HibernateAwareObjectMapper() {
        registerModule(new Hibernate6Module());
    }
}
