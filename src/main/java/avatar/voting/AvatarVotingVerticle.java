package avatar.voting;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AvatarVotingVerticle extends Verticle {

    @Override
    public void start() {
        EventBus eventBus = vertx.eventBus();
        VoterRepository voterRepository = new VoterRepository(eventBus);
        SuggestionRepository suggestionRepository = new SuggestionRepository(eventBus);

        RouteMatcher routeMatcher = new RouteMatcher()
            .optionsWithRegEx("\\/([^\\\\/]+)", req ->
                req.response()
                        .putHeader("Access-Control-Allow-Headers",
                                "Origin, X-Requested-With, Content-Type, Accept")
                        .putHeader("Access-Control-Allow-Origin", "*")
                        .end()
            )

            .post("/login", allowCors(req -> req.bodyHandler(reqBody -> {
                JsonObject reqJson = new JsonObject(reqBody.toString());
                voterRepository.save(new Voter(reqJson.getString("email")), voterReturned ->
                        req.response().end(toJson(voterReturned.get()).toString()));
            })))

            .get("/suggestions", allowCors(req -> suggestionRepository.findAll(suggestions -> {
                List<Map<String, Object>> rets = suggestions.stream().map(
                        suggest -> toMap(suggest)).collect(Collectors.toList());
                req.response().end(new JsonArray(rets).toString());
            })))

            .post("/suggestions", allowCors(req -> req.bodyHandler(reqBody -> {
                JsonObject reqJson = new JsonObject(reqBody.toString());
                String suggestionName = reqJson.getString("suggestion");
                suggestionRepository.findByName(suggestionName, suggestionOptional -> {
                    suggestionOptional.ifPresent(suggest -> {
                        throw new RuntimeException("Suggested name exists!");
                    });
                    suggestionRepository.save(new Suggestion(suggestionName), suggestionReturned -> {
                        eventBus.publish("suggestion", toJson(suggestionReturned.get()));
                        req.response().end("{}");
                    });
                });
            })))

            .post("/vote", allowCors(req -> req.bodyHandler(reqBody -> {
                JsonObject reqJson = new JsonObject(reqBody.toString());
                String suggestionName = reqJson.getString("suggestion");
                suggestionRepository.findByName(suggestionName, suggestionOptional -> {
                    suggestionOptional.orElseThrow(() -> new RuntimeException("Suggested name does not exist!"));
                    Suggestion suggestion = suggestionOptional.get();
                    suggestion.voted();
                    suggestionRepository.updateVotes(suggestion);
                    eventBus.publish("suggestion", toJson(suggestion));
                    req.response().end("{}");
                });
            })));

        HttpServer httpServer = vertx.createHttpServer().requestHandler(routeMatcher);

        JsonObject config = new JsonObject().putString("prefix", "/avatar/voting");
        JsonArray noPermitted = new JsonArray();
        noPermitted.add(new JsonObject());
        vertx.createSockJSServer(httpServer).bridge(config, noPermitted, noPermitted);

        httpServer.listen(9090, asyncResult -> {
            if (asyncResult.failed()) {
                throw new RuntimeException("Unable to start a server", asyncResult.cause());
            }
        });
    }

    private JsonObject toJson(Voter voter) {
        return new JsonObject()
                .putNumber("id", voter.getId())
                .putString("email", voter.getEmail());
    }

    private JsonObject toJson(Suggestion suggestion) {
        return new JsonObject()
                .putNumber("id", suggestion.getId())
                .putString("name", suggestion.getName())
                .putNumber("votes", suggestion.getVotes());
    }

    private Map<String, Object> toMap(Suggestion suggest) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("id", suggest.getId());
        ret.put("name", suggest.getName());
        ret.put("votes", suggest.getVotes());
        return ret;
    }

    private Handler<HttpServerRequest> allowCors(Handler<HttpServerRequest> handler) {
        return req -> {
            req.response().putHeader("Access-Control-Allow-Origin", "*");
            handler.handle(req);
        };
    }
}
