FROM debian:buster-slim
RUN apt update && apt-get install -y libcurl4

ENV USER=user
RUN groupadd $USER \
    && useradd -g $USER $USER \
    && mkdir app
WORKDIR /app
COPY target/release/vocab-mate /app/vocab-mate
COPY config /app/config
RUN chown -R $USER:$USER /app
USER $USER

EXPOSE 8080
ENV RUST_LOG=debug
ENTRYPOINT [ "./vocab-mate" ]