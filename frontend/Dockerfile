# ── stage 1: build ───────────────────────────────────────────────────────────
FROM node:22-alpine AS build
WORKDIR /app

# copia os manifests primeiro para cachear as dependências
COPY package*.json ./
RUN npm ci

# compila TypeScript + Vite
COPY . .
RUN npm run build

# ── stage 2: serve ───────────────────────────────────────────────────────────
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
