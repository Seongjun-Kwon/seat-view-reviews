FROM eclipse-temurin:17
ARG JAR_FILE=build/libs/seat-view-reviews-0.0.1-SNAPSHOT.jar
ENV DB_URL=${DB_URL} \
    DB_USERNAME=${DB_USERNAME} \
    DB_PASSWORD=${DB_PASSWORD} \
    ACCESS_KEY=${ACCESS_KEY} \
    SECRET_KEY=${SECRET_KEY}
COPY ${JAR_FILE} seat-view-reviews.jar
ENTRYPOINT ["nohup", "java", "-jar", "seat-view-reviews.jar", "> /dev/null 2> /dev/null &"]
