package avatar.voting;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import static avatar.voting.StarterVerticle.AVATAR_PERSISTOR_ADDRESS;

public class SuggestionRepository {

    private EventBus eventBus;

    public SuggestionRepository(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void findAll(Consumer<Collection<Suggestion>> returnCallback) {

        JsonObject message = new JsonObject()
                .putString("action", "select")
                .putString("stmt", "SELECT * FROM suggestion ORDER BY id DESC");

        Collection<Suggestion> suggestions = new ArrayList<>();
        eventBus.send(AVATAR_PERSISTOR_ADDRESS, message, (Message<JsonObject> reply) -> {
            JsonObject replyBody = reply.body();
            assertReplyStatus(replyBody, "Unable to find all suggestions");
            JsonArray result = replyBody.getArray("result");
            result.forEach(obj -> suggestions.add(toSuggestion((JsonObject) obj)));
            returnCallback.accept(suggestions);
        });
    }

    public void save(Suggestion suggested, Consumer<Optional<Suggestion>> returnCallback) {

        JsonObject message = new JsonObject()
                .putString("action", "insert")
                .putString("stmt", "INSERT INTO suggestion (name, votes) VALUES (?, ?)")
                .putArray("values", new JsonArray()
                        .addArray(new JsonArray()
                                .addString(suggested.getName())
                                .addNumber(suggested.getVotes())));

        eventBus.send(AVATAR_PERSISTOR_ADDRESS, message, (Message<JsonObject> reply) -> {
            assertReplyStatus(reply.body(), "Unable to insert a suggestion");
            findByName(suggested.getName(), returnCallback);
        });
    }

    public void updateVotes(Suggestion suggested) {

        JsonObject message = new JsonObject()
                .putString("action", "update")
                .putString("stmt", "UPDATE suggestion SET votes = votes + 1 WHERE ID = ?")
                .putArray("values", new JsonArray()
                        .addArray(new JsonArray().addNumber(suggested.getId())));

        eventBus.send(AVATAR_PERSISTOR_ADDRESS, message, (Message<JsonObject> reply) -> {
            assertReplyStatus(reply.body(), "Unable to update a suggestion");
            reply.fail(1, "Unable to update a suggestion");
        });
    }

    public void findByName(String suggestionName, Consumer<Optional<Suggestion>> returnCallback) {

        JsonObject message = new JsonObject()
                .putString("action", "select")
                .putString("stmt", "SELECT * FROM suggestion WHERE name = ?")
                .putArray("values", new JsonArray()
                        .addArray(new JsonArray()
                                .addString(suggestionName)));

        eventBus.send(AVATAR_PERSISTOR_ADDRESS, message, (Message<JsonObject> reply) -> {
            JsonObject replyBody = reply.body();
            assertReplyStatus(replyBody, "Unable to find a suggestion by name");
            JsonArray result = replyBody.getArray("result");
            if (result.size() > 0) {
                returnCallback.accept(Optional.of(toSuggestion(result.get(0))));
            } else {
                returnCallback.accept(Optional.empty());
            }
        });
    }

    private Suggestion toSuggestion(JsonObject json) {
        return new Suggestion(json.getLong("ID"), json.getString("NAME"),
                json.getInteger("VOTES"));
    }

    private void assertReplyStatus(JsonObject replyBody, String message) {
        if (!replyBody.getString("status").equals("ok")) {
            throw new RuntimeException(message);
        }
    }
}
