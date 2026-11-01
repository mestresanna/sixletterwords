package be.anna.sixletterwords.adapter.in.web;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class WordListParser {

    private static finl Pattern WHITESPACE = Pattern.compile("\\s+");

    public List<String> parse(String body) {
        if (body == null || body.isBlank()) {
            return List.of();
        }
        return Arrays.stream(WHITESPACE.split(body.strip()))
                .map(word -> word.toLowerCase(Locale.ROOT))
                .toList();
    }
}

