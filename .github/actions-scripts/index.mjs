import { Octokit } from "octokit";

const octokit = new Octokit({
  auth: process.env.TOKEN
});

try {
  const result = await octokit.rest.issues.createComment({
      owner: "FarhatRV",
      repo: "github-action-test",
      issue_number: 4,
      body: "PR for testing github issue comments."
    });

  console.log(result);

} catch (error) {
  console.log(`Error! Status: ${error.status}. Message: ${error.response.data.message}`)
}