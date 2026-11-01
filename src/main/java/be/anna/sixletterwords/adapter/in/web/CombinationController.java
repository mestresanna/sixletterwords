package be.anna.sixletterwords.adapter.in.web;

import be.anna.sixletterwords.domain.Combination;
import be.anna.sixletterwords.domain.CombinationRules;
import be.anna.sixletterwords.port.in.FindCombinationUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CombinationController {

    private static final Logger log = LoggerFactory.getLogger(CombinationController.class);
    private static final String LINE_SEPARATOR = "\n";

    private final FindCombinationUseCase findCombinationUseCase;
    private final WordListParser parser;
    private final PlusEqualsFormatter formatter;

    public CombinationController(FindCombinationUseCase findCombinationUseCase,
                                 WordListParser parser,
                                 PlusEqualsFormatter formatter) {
        this.findCombinationUseCase = findCombinationUseCase;
        this.parser = parser;
        this.formatter = formatter;
        // Logged once at startup: if this line is missing, Spring never created the controller.
        log.debug("CombinationController created, serving POST /api/file");
    }

    @PostMapping(
            value = "/file",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String findCombinations(@RequestBody(required = false) String body,
                                   @RequestParam(required = false) Integer targetLength,
                                   @RequestParam(required = false) Integer maxParts) {
        log.debug("POST /api/file received: body length={} chars, targetLength={}, maxParts={}",
                body == null ? 0 : body.length(), targetLength, maxParts);

        CombinationRules rules = toRules(targetLength, maxParts);
        List<String> words = parser.parse(body);
        log.debug("Parsed {} words, using {}", words.size(), rules);

        if (words.isEmpty()) {
            log.debug("Rejecting request: no words in body");
            throw new IllegalArgumentException("The request body must contain at least one word");
        }

        List<Combination> combinations = findCombinationUseCase.findCombinations(words, rules);
        log.debug("Found {} combinations", combinations.size());
        return toText(combinations);
    }

    /** Missing parameters fall back to the default rules; invalid values are rejected by the domain. */
    private static CombinationRules toRules(Integer targetLength, Integer maxParts) {
        int target = targetLength != null ? targetLength : CombinationRules.DEFAULT_TARGET_LENGTH;
        int parts = maxParts != null ? maxParts : target;
        return new CombinationRules(target, parts);
    }

    /** One combination per line, ending with a newline so appending with {@code >> output.txt} works cleanly. */
    private String toText(List<Combination> combinations) {
        return combinations.stream()
                .map(formatter::format)
                .map(line -> line + LINE_SEPARATOR)
                .collect(Collectors.joining());
    }
}