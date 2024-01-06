const axios = require("axios");

// log all the env variables
console.log(`GITHUB_TOKEN: ${process.env.GITHUB_TOKEN}`)
console.log(`SONAR_TOKEN: ${process.env.SONAR_TOKEN}`)
console.log(`SONAR_HOST_URL: ${process.env.SONAR_HOST_URL}`)
console.log(`SONAR_PROJECT_KEY: ${process.env.SONAR_PROJECT_KEY}`)
console.log(`GITHUB_OWNER: ${process.env.GITHUB_OWNER}`)
console.log(`GITHUB_REPO: ${process.env.GITHUB_REPO}`)
console.log(`PULL_REQUEST_NUMBER: ${process.env.PULL_REQUEST_NUMBER}`)

// SonarQube API details
const sonarqubeURL = `${process.env.SONAR_HOST_URL}/api`;
const measuresEndpoint = `${sonarqubeURL}/measures/search`;

// SonarQube project and metrics details
const projectKey = process.env.SONAR_PROJECT_KEY;
const metricKeys = [
  "alert_status",
  "bugs",
  "code_smells",
  "reliability_rating",
  "vulnerabilities",
  "security_rating",
  // Add more metric keys as needed
];

// SonarQube authentication token
const sonarToken = process.env.SONAR_TOKEN;

// GitHub repository details
const owner = process.env.GITHUB_OWNER;
const repo = process.env.GITHUB_REPO;
const issueNumber = process.env.PULL_REQUEST_NUMBER;

// Personal access token with appropriate permissions
const gitHubAccessToken = process.env.GITHUB_TOKEN;

// Function to create a comment with a table of metrics on a GitHub issue
async function createCommentWithMetricsTable(metrics) {
  // format metrics as a table
  const tableHeader = "| Metric | Value |\n| ------ | ----- |";
  const tableRows = metrics
    .map((metric) => `| ${metric.key} | ${metric.value} |`)
    .join("\n");

  const commentBody = `${tableHeader}\n${tableRows}`;

  // Create a comment on the GitHub issue
  const apiUrl = `https://api.github.com/repos/${owner}/${repo}/issues/${issueNumber}/comments`;

  // Axios configuration
  const axiosConfig = {
    headers: {
      Authorization: `Bearer ${gitHubAccessToken}`,
      Accept: "application/vnd.github.v3+json",
      "X-GitHub-Api-Version": "2022-11-28",
    },
  };

  try {
    const response = await axios.post(
      apiUrl,
      { body: commentBody },
      axiosConfig
    );
    console.log("Comment created successfully:", response.data);
  } catch (error) {
    console.error(
      "Error creating comment:",
      error?.response?.data || error.message
    );
    throw error;
  }
}

// Function to fetch metrics data from SonarQube API
async function fetchMetricsData() {
  const params = {
    projectKeys: projectKey,
    metricKeys: metricKeys.join(),
  };

  const axiosConfig = {
    headers: {
      Authorization: `Bearer ${sonarToken}`, // Set SonarQube token in the headers
    },
  };

  try {
    const response = await axios.get(measuresEndpoint, {
      params,
      ...axiosConfig,
    });
    const metrics = response.data.measures.map((metric) => ({
      key: metric.metric,
      value: metric.value,
    }));
    await createCommentWithMetricsTable(metrics);
  } catch (error) {
    console.error(
      "Error fetching metrics data:",
      error?.response?.data || error.message
    );
    throw error;
  }
}

// Call the function to fetch metrics data and create a comment with the metrics table
fetchMetricsData();
