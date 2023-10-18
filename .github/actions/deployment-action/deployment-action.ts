import * as core from "@actions/core";
import * as github from "@actions/github";

const allowed = {
  branch: ["master"],
  prefix: ["release/"],
};

try {
  const ref = github.context.payload.pull_request?.["head"]["ref"] as string | null;
  if (ref == null) {
    throw new Error("The branch name has not been recognized.");
  }

  if (allowed.branch.includes(ref) == false && allowed.prefix.includes(ref) == false) {
    const message = `Allowing deployment from branch='${allowed.branch.join(', ')}' or with prefix='${allowed.prefix.join(', ')}'.`;
    core.setFailed(`'${ref}' branch can't be released. ${message}.`);
  }
} catch (error) {
  core.setFailed(error.message);
}
