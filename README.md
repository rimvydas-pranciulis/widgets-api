# Widgets API

## Prerequisites forUsing Application
- Docker
- Java 21 (for development only)

## Starting Application
Application can be started in two ways:
1) Packaged application
   - Start application and database in Docker by running: `docker compose -f docker-compose-full-app.yaml up`
2) Code Editor, i.e. Intellij
    - Start database in Docker by running `docker compose -f docker-compose-db.yaml up`
    - Start application from your code editor

## Using Application
After starting application go to http://localhost:8080/swagger-ui/index.html to see API definition and test it.

## Architecture Decisions
#### Database
Decision was to use PostgreSQL as database, because it was best fit from databases that I have experience with.
It is performant enough for handling predicted number of transactions for this API, especially if using some well setup cluster like AWS Aurora.

## TODOs:
Items that were not completed because of lack of time:
- More tests, including component level test with test containers. Now tests covers only most complex part - connecting widgets. 
- Json response for success and error scenarios 
