package be.anna.sixletterwords.adapter.in.web;

import be.anna.sixletterwords.domain.Combination;
import org.springframework.stereotype.Component;

@Component
public class PlusEqualsFormatter {
    public String format(Combination combination) {
        return String.join("+", combination.parts()) + "=" + combination.result();
    }

}
