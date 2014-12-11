package avatar.voting.domain;

import java.util.List;
import java.util.stream.Collectors;

public class SuggestionDetails {
    private Long id;
    private String name;
    private Long suggesterId;
    private String suggesterEmail;
    private List<Long> voters;

    public SuggestionDetails(Suggestion suggestion) {
        this.id = suggestion.getId();
        this.name = suggestion.getName();
        this.suggesterId = suggestion.getSuggesterId();
        this.suggesterEmail = suggestion.getSuggesterEmail();
        this.voters = suggestion.getVoters().stream().map(Voter::getId).collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSuggesterEmail() {
        return suggesterEmail;
    }

    public Long getSuggesterId() {
        return suggesterId;
    }

    public List<Long> getVoters() {
        return voters;
    }
}
