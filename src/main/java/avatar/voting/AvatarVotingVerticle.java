package avatar.voting;

import avatar.voting.domain.AvatarDetails;
import avatar.voting.domain.SuggestionDetails;
import avatar.voting.domain.VoterDetails;
import avatar.voting.service.AvatarVotingService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
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
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class AvatarVotingVerticle extends Verticle {

    @Override
    public void start() {

        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        AvatarVotingService service = context.getBean(AvatarVotingService.class);

        EventBus eventBus = vertx.eventBus();
        RouteMatcher routeMatcher = new RouteMatcher()

        .post("/avatar", acceptJson((req, reqJson) -> {
            AvatarDetails newAvatar = service.saveAvatar(
                    reqJson.getString("candidate"), reqJson.getString("candidateEmail"));
            req.response().end(jsonString(newAvatar));
        }))

        .post("/avatar-delete", acceptJson((req, reqJson) -> {
            service.deleteAvatar(reqJson.getLong("avatarId"));
            req.response().end("{}");
        }))

        .post("/avatar-suggestion-control", acceptJson((req, reqJson) -> {
            AvatarDetails avatar = service.controlSuggestionOpen(
                    reqJson.getLong("avatarId"), reqJson.getBoolean("suggestionOpen"));
            req.response().end(new JsonObject()
                    .putString("suggestionOpen", avatar.getSuggestionOpenString()).toString());
        }))

        .post("/avatar-vote-control", acceptJson((req, reqJson) -> {
            AvatarDetails avatar = service.controlVoteOpen(
                    reqJson.getLong("avatarId"), reqJson.getBoolean("voteOpen"));
            req.response().end(new JsonObject()
                    .putString("voteOpen", avatar.getVoteOpenString()).toString());
        }))

        .get("/avatar", acceptJson((req, reqJson) -> {
            AvatarDetails avatar = service.findOne(reqJson.getLong("id"));
            req.response().end(jsonString(avatar));
        }))

        .get("/avatar-last", cors(req -> {
            AvatarDetails avatar = service.findLast();
            req.response().end(jsonString(avatar));
        }))

        .get("/avatars", cors(req -> {
            List<Map<String, Object>> listMap = service.findAll()
                    .stream().map(this::toMap)
                    .collect(Collectors.toList());
            req.response().end(new JsonArray(listMap).toString());
        }))

        .post("/login", acceptJson((req, reqJson) -> {
            VoterDetails voter = service.addVoter(
                    reqJson.getLong("avatarId"), reqJson.getString("email"));
            req.response().end(jsonString(voter));
        }))

        .get("/suggestions", cors(req -> {
            List<Map<String, Object>> listMap = service.findSuggestions(
                    Long.parseLong(req.params().get("avatarId")))
                    .stream().map(this::toMap)
                    .collect(Collectors.toList());
            req.response().end(new JsonArray(listMap).toString());
        }))

        .post("/suggestions", acceptJson((req, reqJson) -> {
            SuggestionDetails suggestion = service.addSuggestion(
                    reqJson.getLong("avatarId"), reqJson.getLong("voterId"), reqJson.getString("suggestion"));
            eventBus.publish("suggestion", jsonString(suggestion));
            req.response().end("{}");
        }))

        .post("/vote", acceptJson((req, reqJson) -> {
            SuggestionDetails suggestion = service.voteSuggestion(
                    reqJson.getLong("avatarId"), reqJson.getLong("voterId"), reqJson.getLong("suggestionId"));
            eventBus.publish("suggestion", jsonString(suggestion));
            req.response().end("{}");
        }))

        .optionsWithRegEx("\\/([^\\\\/]+)", req ->
                        req.response()
                                .putHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end()
        );

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

    private String jsonString(AvatarDetails avatar) {
        JsonObject json = new JsonObject();
        if (avatar == null) {
            return json.toString();
        }
        return new JsonObject()
                .putNumber("id", avatar.getId())
                .putString("name", avatar.getName())
                .putString("candidate", avatar.getCandidate())
                .putString("candidateEmail", avatar.getCandidateEmail())
                .putBoolean("suggestionOpen", avatar.getSuggestionOpen())
                .putBoolean("voteOpen", avatar.getVoteOpen())
                .putString("suggestionOpenString", avatar.getSuggestionOpenString())
                .putString("voteOpenString", avatar.getVoteOpenString())
                .toString();
    }

    private String jsonString(VoterDetails voter) {
        return new JsonObject()
                .putNumber("id", voter.getId())
                .putString("email", voter.getEmail())
                .toString();
    }

    private String jsonString(SuggestionDetails suggestion) {
        return new JsonObject()
                .putNumber("id", suggestion.getId())
                .putString("name", suggestion.getName())
                .putArray("voters", new JsonArray(suggestion.getVoters()))
                .putString("suggesterEmail", suggestion.getSuggesterEmail())
                .putNumber("suggesterId", suggestion.getSuggesterId())
                .toString();
    }

    private Map<String, Object> toMap(AvatarDetails avatar) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("id", avatar.getId());
        ret.put("name", avatar.getName());
        ret.put("candidate", avatar.getCandidate());
        ret.put("candidateEmail", avatar.getCandidateEmail());
        ret.put("suggestionOpen", avatar.getSuggestionOpen());
        ret.put("voteOpen", avatar.getVoteOpen());
        ret.put("voters", avatar.getNumberOfVoters());
        ret.put("suggestions", avatar.getNumberOfSuggestions());
        return ret;
    }

    private Map<String, Object> toMap(SuggestionDetails suggest) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("id", suggest.getId());
        ret.put("name", suggest.getName());
        ret.put("voters", suggest.getVoters());
        return ret;
    }

    private void badRequest(HttpServerRequest req, String message) {
        req.response()
            .setStatusCode(400)
            .setStatusMessage(message)
            .end();
    }

    private Handler<HttpServerRequest> acceptJson(BiConsumer<HttpServerRequest, JsonObject> handler) {
        return cors(req -> req.bodyHandler(body -> {
            System.out.println("req uri: " + req.uri() + ", json: " + body.toString());
            try {
                handler.accept(req, new JsonObject(body.toString()));
            } catch (Exception e) {
                System.out.println("Got an error: " + e.getMessage());
                badRequest(req, e.getMessage());
            }
        }));
    }

    private Handler<HttpServerRequest> cors(Handler<HttpServerRequest> handler) {
        return req -> {
            req.response().putHeader("Access-Control-Allow-Origin", "*");
            try {
                handler.handle(req);
            } catch (Exception e) {
                System.out.println("Got an error: " + e.getMessage());
                badRequest(req, e.getMessage());
            }
        };
    }
}
