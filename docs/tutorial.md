./mvnw flyway:migrate     # apply migrations
./mvnw flyway:info        # show migration status
./mvnw flyway:validate    # validate applied vs local
./mvnw flyway:clean       # drop all objects (dev only!)