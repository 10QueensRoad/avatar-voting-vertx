# Avatar voting back-end vert.x module to support a push message functionality.

Just did a spike for vertx framework which is based on Netty framework and supports the polyglot which enables developers to choose their preferred languages to develop back end protocol services.
- http://vertx.io/

The following article is the comparison between vertx and node.js.
- http://www.cubrid.org/blog/dev-platform/inside-vertx-comparison-with-nodejs/

UI project for avatar voting is 'avatar-voting-system' project using polymer.
- https://github.com/10QueensRoad/avatar-voting-system

However, currently the branch 'avatar-voting-system-vertxbus' of the project above should be used to interact with this vertx module.
In order to run avatar-voting-vertx module, run the following gradle command.

- gradlew clean build runMod