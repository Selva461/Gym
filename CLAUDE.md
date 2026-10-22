# Working on this repo

- **Read `handoff.md` first.** It says how the code reaches the phone, how to set
  up a session, and what is open.
- **Every commit that changes code, data, the build or the plan also updates
  `handoff.md`** in the same commit: the current-state table, counts, pipeline
  status, open items, anything a fresh session would need. A session should be
  able to start from `handoff.md` alone.
- Follow the existing flow rather than inventing one: work on the `claude/...`
  branch, push it (Android CI publishes the APK), merge to `main` when verified.
