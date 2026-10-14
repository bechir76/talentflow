FROM node:24-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# Vite bakes the browser-facing Keycloak URL into the bundle.
ARG VITE_KEYCLOAK_URL=http://localhost:8180
ENV VITE_KEYCLOAK_URL=$VITE_KEYCLOAK_URL
ARG VITE_COMPANY_NAME="Acme GmbH"
ENV VITE_COMPANY_NAME=$VITE_COMPANY_NAME
RUN npm run build

FROM nginx:1.31-alpine
ENV KEYCLOAK_URL=http://localhost:8180
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY nginx/security-headers.conf /etc/nginx/snippets/security-headers.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
