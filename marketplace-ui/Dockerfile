# Stage 1: Build Angular app
FROM node:20-alpine AS build-angular
ARG BUILD_ENV, GITHUB_CLIENT_ID
WORKDIR /app
COPY . .
# Replace the placeholder in the environment file during build
RUN sed -i "s|GITHUB_CLIENT_ID|${GITHUB_CLIENT_ID}|g" src/environments/environment.staging.ts
RUN npm install
RUN npm run build -- --configuration $BUILD_ENV

# Stage 2: Serve Angular app using Nginx
FROM nginx
COPY --from=build-angular /app/dist/browser /usr/share/nginx/html

# Change ownership of the shared volume
RUN mkdir -p /usr/share/nginx/html/market-cache
RUN chown -R www-data:www-data /usr/share/nginx/html/market-cache
RUN chmod -R 755 /usr/share/nginx/html/market-cache
RUN chmod g+s /usr/share/nginx/html/market-cache


