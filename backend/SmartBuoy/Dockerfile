FROM gradle:8.14.3-jdk21

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle

RUN gradle dependencies --no-daemon || true

COPY . .

EXPOSE 8000

CMD ["gradle", "bootRun", "--no-daemon"]
