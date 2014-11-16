package avatar.voting;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Optional;
import java.util.function.Consumer;

import static avatar.voting.StarterVerticle.AVATAR_PERSISTOR_ADDRESS;

public class VoterRepository {
    private EventBus eventBus;

    public VoterRepository(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void save(Voter voter, Consumer<Optional<Voter>> returnCallback) {

        JsonObject message = new JsonObject()
                .putString("action", "insert")
                .putString("stmt", "INSERT INTO voter (email) VALUES (?)")
                .putArray("values", new JsonArray()
                        .addArray(new JsonArray().addString(voter.getEmail())));

        eventBus.send(AVATAR_PERSISTOR_ADDRESS, message, (Message<JsonObject> reply) -> {
            assertReplyStatus(reply.body(), "Unable to insert a voter");
            findByEmail(voter.getEmail(), returnCallback);
        });
    }

    public void findById(Long id, Consumer<Optional<Voter>> returnCallback) {

        JsonObject message = new JsonObject()
                .putString("action", "select")
                .putString("stmt", "SELECT * FROM voter WHERE id = ?")
                .putArray("values", new JsonArray()
                        .addArray(new JsonArray().addNumber(id)));

        eventBus.send(AVATAR_PERSISTOR_ADDRESS, message, (Message<JsonObject> reply) -> {
                    JsonObject replyBody = reply.body();
            assertReplyStatus(replyBody, "Unable to find a voter by id");
            JsonArray result = replyBody.getArray("result");
            if (result.size() > 0) {
                returnCallback.accept(Optional.of(toVoter(result.get(0))));
            } else {
                returnCallback.accept(Optional.empty());
            }
        });
    }

    public void findByEmail(String email, Consumer<Optional<Voter>> returnCallback) {

        JsonObject message = new JsonObject()
                .putString("action", "select")
                .putString("stmt", "SELECT * FROM voter WHERE email = ?")
                .putArray("values", new JsonArray()
                        .addArray(new JsonArray().addString(email)));

        eventBus.send(AVATAR_PERSISTOR_ADDRESS, message, (Message<JsonObject> reply) -> {
            JsonObject replyBody = reply.body();
            assertReplyStatus(replyBody, "Unable to find a voter by email");
            JsonArray result = replyBody.getArray("result");
            if (result.size() > 0) {
                returnCallback.accept(Optional.of(toVoter(result.get(0))));
            } else {
                returnCallback.accept(Optional.empty());
            }
        });
    }

    private Voter toVoter(JsonObject retJson) {
        return new Voter(retJson.getLong("ID"), retJson.getString("EMAIL"));
    }

    private void assertReplyStatus(JsonObject replyBody, String message) {
        if (!replyBody.getString("status").equals("ok")) {
            throw new RuntimeException(message);
        }
    }
}
