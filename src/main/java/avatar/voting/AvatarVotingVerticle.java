package avatar.voting;

import avatar.voting.domain.Avatar;
import avatar.voting.domain.Suggestion;
import avatar.voting.domain.Voter;
import avatar.voting.repository.AvatarRepository;
import avatar.voting.repository.SuggestionRepository;
import avatar.voting.repository.VoterRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
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
        AvatarRepository avatarRepository = context.getBean(AvatarRepository.class);
        VoterRepository voterRepository = context.getBean(VoterRepository.class);
        SuggestionRepository suggestionRepository = context.getBean(SuggestionRepository.class);

        EventBus eventBus = vertx.eventBus();
        RouteMatcher routeMatcher = new RouteMatcher()

        .post("/login", acceptJson((req, reqJson) -> {
            Avatar avatar = avatarRepository.findOne(reqJson.getLong("avatarId"));
            Voter voter = new Voter(reqJson.getString("email"));
            avatar.addVoter(voter);
            avatarRepository.saveAndFlush(avatar);
            req.response().end(jsonString(voter));
        }))

        .post("/avatar", acceptJson((req, reqJson) -> {
            Avatar avatar = avatarRepository.findByCandidateEmail(reqJson.getString("candidateEmail"));
            if (avatar != null) {
                badRequest(req, "Avatar for the candidate exists!");
                return;
            }
            Avatar newAvatar = new Avatar(reqJson.getString("candidate"), reqJson.getString("candidateEmail"));
            avatarRepository.save(newAvatar);
            req.response().end(jsonString(newAvatar));
        }))

        .post("/avatar-delete", acceptJson((req, reqJson) -> {
            avatarRepository.delete(reqJson.getLong("avatarId"));
            req.response().end("{}");
        }))

        .post("/avatar-suggestion-control", acceptJson((req, reqJson) -> {
            Avatar avatar = avatarRepository.findOne(reqJson.getLong("avatarId"));
            avatar.setSuggestionOpen(reqJson.getBoolean("suggestionOpen"));
            avatarRepository.saveAndFlush(avatar);
            req.response().end(new JsonObject()
                    .putBoolean("suggestionOpen", avatar.getSuggestionOpen()).toString());
        }))

        .post("/avatar-vote-control", acceptJson((req, reqJson) -> {
            Avatar avatar = avatarRepository.findOne(reqJson.getLong("avatarId"));
            avatar.setVoteOpen(reqJson.getBoolean("voteOpen"));
            req.response().end(new JsonObject()
                    .putBoolean("voteOpen", avatar.getVoteOpen()).toString());
        }))

        .post("/avatar-vote-control", acceptJson((req, reqJson) -> {
            avatarRepository.delete(reqJson.getLong("avatarId"));
            req.response().end("{}");
        }))

        .get("/avatar", acceptJson((req, reqJson) -> {
            Avatar avatar = avatarRepository.findOne(reqJson.getLong("id"));
            if (avatar == null) {
                badRequest(req, "Avatar does not exist.");
                return;
            }
            req.response().end(jsonString(avatar));
        }))

        .get("/avatar-last", cors(req -> {
            Avatar avatar = avatarRepository.findLast();
            req.response().end(jsonString(avatar));
        }))

        .get("/avatars", cors(req -> {
            List<Map<String, Object>> listMap = avatarRepository.findAll(
                    new Sort(new Sort.Order(Sort.Direction.DESC, "id")))
                    .stream().map(this::toMap)
                    .collect(Collectors.toList());
            req.response().end(new JsonArray(listMap).toString());
        }))

        .get("/voter", acceptJson((req, reqJson) -> {
            Voter voter = voterRepository.findOne(reqJson.getLong("id"));
            if (voter == null) {
                badRequest(req, "Voter does not exist. Please do login again!");
                return;
            }
            req.response().end(jsonString(voter));
        }))

        .get("/suggestions", acceptJson((req, reqJson) -> {
            Avatar avatar = avatarRepository.findOne(reqJson.getLong("avatarId"));
            if (avatar == null) {
                badRequest(req, "Avatar does not exist.");
                return;
            }
            List<Map<String, Object>> listMap = avatar.getSuggestions()
                    .stream().map(this::toMap)
                    .collect(Collectors.toList());
            req.response().end(new JsonArray(listMap).toString());
        }))

        .post("/suggestions", acceptJson((req, reqJson) -> {
            Avatar avatar = avatarRepository.findOne(reqJson.getLong("avatarId"));
            if (avatar == null) {
                badRequest(req, "Avatar does not exist.");
                return;
            }
            Voter voter = voterRepository.findOne(reqJson.getLong("voterId"));
            Suggestion suggestion = avatar.addSuggestion(voter, reqJson.getString("suggestion"));
            eventBus.publish("suggestion", jsonString(suggestion));
            req.response().end("{}");
        }))

        .post("/vote", acceptJson((req, reqJson) -> {
            Avatar avatar = avatarRepository.findOne(reqJson.getLong("avatarId"));
            if (avatar == null) {
                badRequest(req, "Avatar does not exist.");
                return;
            }
            Voter voter = voterRepository.findOne(reqJson.getLong("voterId"));
            Suggestion suggestion = suggestionRepository.findOne(reqJson.getLong("suggestionId"));
            Suggestion votedSuggestion = avatar.vote(suggestion, voter);
            eventBus.publish("suggestion", jsonString(votedSuggestion));
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

    private String jsonString(Avatar avatar) {
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
                .toString();
    }

    private String jsonString(Voter voter) {
        return new JsonObject()
                .putNumber("id", voter.getId())
                .putString("email", voter.getEmail())
                .toString();
    }

    private String jsonString(Suggestion suggestion) {
        return new JsonObject()
                .putNumber("id", suggestion.getId())
                .putString("name", suggestion.getName())
                .putNumber("votes", suggestion.getVotes())
                .putString("suggester", suggestion.getSuggesterEmail())
                .toString();
    }

    private Map<String, Object> toMap(Avatar avatar) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("id", avatar.getId());
        ret.put("name", avatar.getName());
        ret.put("candidate", avatar.getCandidate());
        ret.put("candidateEmail", avatar.getCandidateEmail());
        ret.put("suggestionOpen", avatar.getSuggestionOpen());
        ret.put("voteOpen", avatar.getVoteOpen());
        ret.put("voters", avatar.getVoters().size());
        ret.put("suggestions", avatar.getSuggestions().size());
        return ret;
    }

    private Map<String, Object> toMap(Suggestion suggest) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("id", suggest.getId());
        ret.put("name", suggest.getName());
        ret.put("votes", suggest.getVotes());
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
            System.out.println(body.toString());
            handler.accept(req, new JsonObject(body.toString()));
        }));
    }

    private Handler<HttpServerRequest> cors(Handler<HttpServerRequest> handler) {
        return req -> {
            req.response().putHeader("Access-Control-Allow-Origin", "*");
            handler.handle(req);
        };
    }
}
