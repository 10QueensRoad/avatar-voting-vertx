package avatar.voting.domain;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Entity
public class Avatar {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 500, nullable = true)
    private String name;

    @Column
    private String candidate;

    @Column(name = "candidate_email")
    private String candidateEmail;

    @Column(name = "suggestion_open")
    private Boolean suggestionOpen;

    @Column(name = "vote_open")
    private Boolean voteOpen;

    @OneToMany(mappedBy = "avatar", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<Voter> voters = new LinkedHashSet<>();

    @OneToMany(mappedBy = "avatar", orphanRemoval = true, cascade = CascadeType.ALL)
    @OrderBy("id desc")
    private Set<Suggestion> suggestions = new LinkedHashSet<>();

    private Avatar() {
    }

    public Avatar(String candidate, String candidateEmail) {
        this.candidate = candidate;
        this.candidateEmail = candidateEmail;
        this.suggestionOpen = true;
        this.voteOpen = false;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCandidate() {
        return candidate;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public Boolean getSuggestionOpen() {
        return suggestionOpen;
    }

    public Boolean getVoteOpen() {
        return voteOpen;
    }

    public Set<Voter> getVoters() {
        return voters;
    }

    public Set<Suggestion> getSuggestions() {
        return suggestions;
    }

    public void addVoter(Voter voter) {
        this.voters.add(voter);
    }

    public Suggestion addSuggestion(Long suggesterId, String suggestionName) {
        if (suggestions.stream().anyMatch(suggestion -> suggestion.getName().equals(suggestionName))) {
            throw new RuntimeException("Suggested name exists!");
        }
        Optional<Voter> suggester = findVoter(suggesterId);
        Suggestion suggestion = new Suggestion(suggestionName, suggester.get(), this);
        suggestions.add(suggestion);
        return suggestion;
    }

    public Suggestion vote(Long voterId, Long suggestionId) {
        Optional<Voter> voter = findVoter(voterId);
        Optional<Suggestion> suggestion = findSuggestion(suggestionId);
        Suggestion suggested = suggestion.get();
        suggested.voted(voter.get());
        return suggested;
    }

    public void setSuggestionOpen(Boolean suggestionOpen) {
        this.suggestionOpen = suggestionOpen;
    }

    public void setVoteOpen(Boolean voteOpen) {
        this.voteOpen = voteOpen;
    }

    private Optional<Voter> findVoter(Long voterId) {
        System.out.println("voters:" + getVoters());
        System.out.println("voter size:" + getVoters().size());

        Optional<Voter> suggester = getVoters().stream().filter(
                voter -> voter.getId().equals(voterId)).findFirst();
        if (!suggester.isPresent()) {
            throw new RuntimeException("Voter doest not exist!");
        }
        return suggester;
    }

    private Optional<Suggestion> findSuggestion(Long suggestionId) {
        Optional<Suggestion> suggestion = suggestions.stream().filter(
                suggest -> suggest.getId().equals(suggestionId)).findFirst();
        if (!suggestion.isPresent()) {
            throw new RuntimeException("Suggestion does not exist");
        }
        return suggestion;
    }

}
