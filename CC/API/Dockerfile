FROM node:20-buster

WORKDIR /app

COPY package* .
RUN npm install 

COPY . . 

ENV PORT=8080
EXPOSE 8080

CMD ["npm", "run", "start"]
