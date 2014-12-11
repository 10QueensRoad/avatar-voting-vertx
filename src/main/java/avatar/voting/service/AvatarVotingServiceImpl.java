package avatar.voting.service;

import avatar.voting.domain.*;
import avatar.voting.repository.AvatarRepository;
import avatar.voting.repository.SuggestionRepository;
import avatar.voting.repository.VoterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AvatarVotingServiceImpl implements AvatarVotingService {
    private final AvatarRepository avatarRepository;
    private final VoterRepository voterRepository;
    private final SuggestionRepository suggestionRepository;

    @Autowired
    public AvatarVotingServiceImpl(AvatarRepository avatarRepository,
                                   VoterRepository voterRepository,
                                   SuggestionRepository suggestionRepository) {
        this.avatarRepository = avatarRepository;
        this.voterRepository = voterRepository;
        this.suggestionRepository = suggestionRepository;
    }

    @Override
    public AvatarDetails saveAvatar(String candidate, String candidateEmail) {
        Avatar avatar = avatarRepository.findByCandidateEmail(candidateEmail);
        if (avatar != null) {
            throw new RuntimeException("Avatar for the candidate exists!");
        }
        Avatar newAvatar = new Avatar(candidate, candidateEmail);
        avatarRepository.save(newAvatar);
        return new AvatarDetails(newAvatar);
    }

    @Override
    public void deleteAvatar(Long avatarId) {
        avatarRepository.delete(avatarId);
    }

    @Override
    public AvatarDetails controlSuggestionOpen(Long avatarId, Boolean suggestionOpen) {
        Avatar avatar = avatarRepository.findOne(avatarId);
        avatar.setSuggestionOpen(suggestionOpen);
        return new AvatarDetails(avatar);
    }

    @Override
    public AvatarDetails controlVoteOpen(Long avatarId, Boolean voteOpen) {
        Avatar avatar = avatarRepository.findOne(avatarId);
        avatar.setVoteOpen(voteOpen);
        return new AvatarDetails(avatar);
    }

    @Override
    public AvatarDetails findOne(Long id) {
        Avatar avatar = avatarRepository.findOne(id);
        assertAvatarExist(avatar);
        return new AvatarDetails(avatar);
    }

    @Override
    public AvatarDetails findLast() {
        Avatar avatar = avatarRepository.findLast();
        return new AvatarDetails(avatar);
    }

    @Override
    public List<AvatarDetails> findAll() {
        return avatarRepository.findAll(new Sort(new Sort.Order(Sort.Direction.DESC, "id")))
                .stream().map(AvatarDetails::new).collect(Collectors.toList());
    }

    @Override
    public VoterDetails addVoter(Long avatarId, String voterEmail) {
        Avatar avatar = avatarRepository.findOne(avatarId);
        Voter voter = new Voter(avatar, voterEmail);
        voterRepository.save(voter);
        return new VoterDetails(voter);
    }

    @Override
    public List<SuggestionDetails> findSuggestions(Long avatarId) {
        Avatar avatar = avatarRepository.findOne(avatarId);
        assertAvatarExist(avatar);
        return avatar.getSuggestions()
                .stream().map(SuggestionDetails::new)
                .collect(Collectors.toList());
    }

    @Override
    public SuggestionDetails addSuggestion(Long avatarId, Long voterId, String suggestionName) {
        Avatar avatar = avatarRepository.findOne(avatarId);
        assertAvatarExist(avatar);
        Suggestion suggestion = avatar.addSuggestion(voterId, suggestionName);
        suggestionRepository.save(suggestion);
        return new SuggestionDetails(suggestion);
    }

    @Override
    public SuggestionDetails voteSuggestion(Long avatarId, Long voterId, Long suggestionId) {
        Avatar avatar = avatarRepository.findOne(avatarId);
        assertAvatarExist(avatar);
        Suggestion votedSuggestion = avatar.vote(voterId, suggestionId);
        return new SuggestionDetails(votedSuggestion);
    }

    private void assertAvatarExist(Avatar avatar) {
        if (avatar == null) {
            throw new RuntimeException("Avatar does not exist.");
        }
    }
}
