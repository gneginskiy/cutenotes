# cuteNotes: the app no longer takes its own saves for outside changes

Date: 2026-09-26 · branch `develop` · `mvn clean install`: all 335 tests pass, 0 checkstyle violations, coverage above the threshold.

## Problem

Right after the release with "reload notes changed on disk", notes sometimes jumped back a few words or got a "(from disk)" copy, with a toast saying the file had changed, even with a single cuteNotes running.

**Cause: a race with the app's own autosave.** Every 2 s `ExternalEdits` looks for open notes whose file time changed; the app's own saves change it too. The watcher then read the file and, as a separate step, asked `AutoSaver` what it saved last. When an autosave landed between the two, the file looked older than the last save. The result:
- a tab without new typing was "reloaded" with the older text (the last words were lost);
- with new typing, the older text was saved as a "(from disk)" copy.

While typing, autosaves run every 300 ms, so this happened within minutes.

**Also found on the machine:** a cuteNotes jar from June (`~/cutenotes-2026-06-18.jar`) was running at the same time and using the same `~/cutenotes_data`. Versions before 2026-09-26 have no single-instance lock, so the new app cannot stop them. Their writes are genuine outside changes: on start that version rewrites every open note, and it never reloads what the new app saved.

## Fix

- `AutoSaver.withoutSaving(read)`: runs a read while no save can happen.
- `ExternalEdits` reads the file and the last save inside it, so both describe the same moment. The app's own writes always compare equal and are ignored.

**Tests:**
- `ExternalEditsTest.theAppsOwnSaveDuringACheckIsNotTakenForAnOutsideChange` forces an autosave between the two reads. It failed before the fix: the tab went back to the older text.
- `AutoSaverTest.readsWithoutSavingGetTheValueOfTheRead`.
