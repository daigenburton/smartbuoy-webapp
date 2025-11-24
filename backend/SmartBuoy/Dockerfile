FROM gradle:8.14.3-jdk21

WORKDIR /app

# Copy gradle files first for better caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies (this layer will be cached)
RUN gradle build --no-daemon || return 0

# Copy source code
COPY . .

# Expose your API port
EXPOSE 8000

# Run the application
CMD ["gradle", "runServer", "--no-daemon"]