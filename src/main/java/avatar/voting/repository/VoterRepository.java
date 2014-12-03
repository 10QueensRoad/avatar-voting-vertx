package avatar.voting.repository;

import avatar.voting.domain.Voter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface VoterRepository extends JpaRepository<Voter, Long> {

}
