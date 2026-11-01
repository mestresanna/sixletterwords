package be.anna.sixletterwords.adapter.in.web;

import be.anna.sixletterwords.domain.Combination;
import be.anna.sixletterwords.domain.CombinationRules;
import be.anna.sixletterwords.port.in.FindCombinationUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CombinationController.class)
@Import({WordListParser.class, PlusEqualsFormatter.class})
class CombinationControllerTest {

    private static final String URL = "/api/file";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindCombinationUseCase findCombinationUseCase;

    @Test
    void returnsOneCombinationPerLineInTheBriefFormat() throws Exception {
        when(findCombinationUseCase.findCombinations(any(), any()))
                .thenReturn(List.of(Combination.of("fo", "obar"), Combination.of("foo", "bar")));

        mockMvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("foobar\nfo\nobar\nfoo\nbar"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("fo+obar=foobar\nfoo+bar=foobar\n"));
    }

    @Test
    void passesTheParsedWordsAndDefaultRulesToTheUseCase() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("foobar\r\nfo\r\n\r\nobar\r\n"))
                .andExpect(status().isOk());

        verify(findCombinationUseCase)
                .findCombinations(List.of("foobar", "fo", "obar"), CombinationRules.defaults());
    }

    @Test
    void usesTheRulesFromTheQueryParameters() throws Exception {
        mockMvc.perform(post(URL).param("targetLength", "4").param("maxParts", "2")
                        .contentType(MediaType.TEXT_PLAIN).content("abcd\nab\ncd"))
                .andExpect(status().isOk());

        verify(findCombinationUseCase)
                .findCombinations(List.of("abcd", "ab", "cd"), new CombinationRules(4, 2));
    }

    @Test
    void returnsAnEmptyBodyWhenNothingIsFound() throws Exception {
        when(findCombinationUseCase.findCombinations(any(), any())).thenReturn(List.of());

        mockMvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("foobar\nxyz"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void rejectsAnEmptyBody() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content(""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The request body must contain at least one word\n"));

        verify(findCombinationUseCase, never()).findCombinations(any(), any());
    }

    @Test
    void rejectsInvalidRules() throws Exception {
        mockMvc.perform(post(URL).param("maxParts", "1")
                        .contentType(MediaType.TEXT_PLAIN).content("foobar\nfo\nobar"))
                .andExpect(status().isBadRequest());

        verify(findCombinationUseCase, never()).findCombinations(any(), any());
    }

    @Test
    void rejectsANonTextContentType() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("[\"foobar\"]"))
                .andExpect(status().isUnsupportedMediaType());
    }
}