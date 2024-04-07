package edu.agh.bpmnai.generator.v2.functions.parameter;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class NullabilityCheck {
    public Set<String> check(Object objectToCheck) {
        Class<?> clazz = objectToCheck.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Set<String> notAnnotatedFieldsWithNullValue = new HashSet<>();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(Nullable.class)) {
                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(objectToCheck);
                    if (fieldValue == null) {
                        notAnnotatedFieldsWithNullValue.add(field.getName());
                    }
                } catch (IllegalAccessException e) {
                    log.warn("Could not access field '{}'", field, e);
                }
            }
        }

        return notAnnotatedFieldsWithNullValue;
    }
}
