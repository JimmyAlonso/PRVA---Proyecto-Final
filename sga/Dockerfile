# =====================================================================================
#  SGA — JiyJu Glamping. Imagen para Render (Guía 22, 5.1). Build multi-etapa.
# =====================================================================================

# ---------- Etapa 1: compilación (JDK completo) ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

# ---------- Etapa 2: ejecución (solo JRE + jar) ----------
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# JVM ajustada a los 512 MB del plan gratuito y zona horaria de Colombia (horas de entrada,
# salida y no-show se evalúan en hora local)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k -Duser.timezone=America/Bogota"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
