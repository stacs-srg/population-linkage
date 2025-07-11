FROM population-linkage-base

# Copy entrypoint
WORKDIR /app
COPY docker/runall/entrypoint.sh .
RUN chmod +x ./entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]