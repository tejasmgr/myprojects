# Step 1: Build the React app
FROM node:20 AS build

# Set working directory inside the container
WORKDIR /app

# Copy package.json and yarn.lock first (for caching)
COPY package.json yarn.lock ./

# Install dependencies
RUN yarn install

# Copy rest of the project
COPY . .

# Build the app for production
RUN yarn build

# Step 2: Serve the built app using nginx
FROM nginx:alpine

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy build output from previous stage
COPY --from=build /app/build /usr/share/nginx/html

# Expose port 80 for nginx
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
