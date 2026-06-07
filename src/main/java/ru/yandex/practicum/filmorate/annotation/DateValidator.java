package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateValidator implements ConstraintValidator<ValidDate, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public boolean isValid(String date, ConstraintValidatorContext context) {
        if (date == null || date.isBlank()) {
            return true;
        }
        try {
            LocalDate.parse(date, FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}