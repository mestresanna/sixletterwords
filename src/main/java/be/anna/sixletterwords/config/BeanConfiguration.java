package be.anna.sixletterwords.config;
import be.anna.sixletterwords.domain.BacktrackingCombinationFinder;
import be.anna.sixletterwords.domain.CombinationFinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {
    @Bean
    public CombinationFinder combinationFinder() {
        return new BacktrackingCombinationFinder();
    }
}