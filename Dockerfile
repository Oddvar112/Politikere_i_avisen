FROM amazoncorretto:17 AS build

RUN dnf install -y maven

WORKDIR /app

COPY pom.xml .
COPY model/pom.xml model/
COPY core/pom.xml core/
COPY dto/pom.xml dto/
COPY analysis/pom.xml analysis/
COPY server/pom.xml server/

RUN mvn dependency:go-offline -B

COPY . .

RUN mvn clean package -DskipTests -B

FROM amazoncorretto:17

RUN dnf install -y python3 python3-pip

WORKDIR /app

COPY --from=build /app/server/target/server-1.0-SNAPSHOT.jar app.jar
COPY --from=build /app/setup/ setup/

RUN pip3 install transformers torch onnx onnxruntime onnxscript requests

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
