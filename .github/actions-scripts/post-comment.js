const axios = require('axios');

// GitHub repository details
const owner = 'FarhatRV';
const repo = 'github-action-test';
const issueNumber = 4; // Replace with the issue number where you want to add the comment

// Personal access token with appropriate permissions
const accessToken = process.env.GITHUB_TOKEN;

// Comment content
const commentBody = 'PR for testing github issue comments.';

// API endpoint for adding a comment to an issue
const apiUrl = `https://api.github.com/repos/${owner}/${repo}/issues/${issueNumber}/comments`;

// Axios configuration
const axiosConfig = {
  headers: {
    Authorization: `Bearer ${accessToken}`,
    Accept: 'application/vnd.github.v3+json',
    "X-GitHub-Api-Version": "2022-11-28"
  },
};

// Create a comment on the GitHub issue
axios.post(apiUrl, { body: commentBody }, axiosConfig)
  .then((response) => {
    console.log('Comment created successfully:', response.data);
  })
  .catch((error) => {
    console.error('Error creating comment:', error.response.data || error.message);
  });
