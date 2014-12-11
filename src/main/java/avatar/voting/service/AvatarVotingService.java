package avatar.voting.service;

import avatar.voting.domain.AvatarDetails;
import avatar.voting.domain.SuggestionDetails;
import avatar.voting.domain.VoterDetails;

import java.util.List;

public interface AvatarVotingService {

    AvatarDetails saveAvatar(String candidate, String candidateEmail);

    void deleteAvatar(Long avatarId);

    AvatarDetails controlSuggestionOpen(Long avatarId, Boolean suggestionOpen);

    AvatarDetails controlVoteOpen(Long avatarId, Boolean voteOpen);

    AvatarDetails findOne(Long id);

    AvatarDetails findLast();

    List<AvatarDetails> findAll();

    VoterDetails addVoter(Long avatarId, String voterEmail);

    List<SuggestionDetails> findSuggestions(Long avatarId);

    SuggestionDetails addSuggestion(Long avatarId, Long voterId, String suggestionName);

    SuggestionDetails voteSuggestion(Long avatarId, Long voterId, Long suggestionId);
}
