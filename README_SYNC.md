## Offline-First Implementation Notes
- Created `VioraDao` and `AppDatabase` with `TaskEntity`, `TeamEntity`, `ListEntity`.
- Created `SyncWorker` to run in background.
- UI triggers `SyncManager.triggerImmediateSync` via top bar.
- API models mapped in `VioraDtos`.
