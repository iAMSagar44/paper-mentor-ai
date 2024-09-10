module.exports = {
  apps: [
    {
      name: "chat-ui-app",
      script: "npm",
      args: "start",
      env: {
        NODE_ENV: "production",
      },
    },
  ],
};