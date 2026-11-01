package be.anna.sixletterwords;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CombinationApiEndToEndTest {

    private static final String URL = "/api/file";
    private static final int TWO_PART_COMBINATIONS = 915;
    private static final int ALL_COMBINATIONS = 4055;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void returnsTheExpectedOutputForTheSampleInput() throws IOException {
        ResponseEntity<String> response = post(URL + "?maxParts=2", readResource("input.txt"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_PLAIN)).isTrue();
        String expected = readResource("expected-output-two-parts.txt");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().lines()).containsExactlyElementsOf(expected.lines().toList());
        assertThat(response.getBody()).endsWith("\n");
    }

    @Test
    void findsCombinationsOfAnyLengthWithoutAPartLimit() throws IOException {
        ResponseEntity<String> response = post(URL, readResource("input.txt"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().lines()).hasSize(ALL_COMBINATIONS);
        assertThat(response.getBody()).contains("a+b+r+o+a+d=abroad\n", "abroa+d=abroad\n");
    }

    @Test
    void storesTheCombinationsInTheDatabase() throws IOException {
        int rowsBefore = countStoredCombinations();

        post(URL + "?maxParts=2", readResource("input.txt"));

        assertThat(countStoredCombinations()).isEqualTo(rowsBefore + TWO_PART_COMBINATIONS);
    }

    @Test
    void rejectsAnEmptyBody() {
        ResponseEntity<String> response = post(URL, "");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("at least one word");
    }

    private ResponseEntity<String> post(String url, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }

    private int countStoredCombinations() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM combination", Integer.class);
        return count == null ? 0 : count;
    }

    private static String readResource(String name) throws IOException {
        return new ClassPathResource(name).getContentAsString(StandardCharsets.UTF_8);
    }
}