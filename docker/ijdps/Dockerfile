FROM population-linkage-base

# Copy entrypoint
WORKDIR /app
COPY docker/ijdps/entrypoint.sh .
RUN chmod +x ./entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]