FROM node:18 AS build-stage

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm install --legacy-peer-deps

COPY . .

RUN npm run build --prod

FROM nginx:alpine AS production-stage

COPY --from=build-stage /app/dist/ensitech-groupe3 /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]