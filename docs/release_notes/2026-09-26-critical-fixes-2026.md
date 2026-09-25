# cuteNotes: critical bug review and fixes

Date: 2026-09-26 · branch `develop` · `mvn clean install`: all 110 tests pass, 0 checkstyle violations, coverage above the threshold.

For every bug, a test that reproduces it was written first, and the fix came after.
The uncommitted changes in `NoteEditor` and `DocumentMarkdown` (clearing image attributes at the caret, skipping duplicate images) were left as they were.

## Summary

| # | Problem | Class | Severity |
|---|---------|-------|----------|
| 1 | The process hangs when the window is closed; the last edits and the open-tabs list are not saved | reliability | 🔴 critical |
| 2 | `*`, `__`, `~~`, ```` ``` ```` in plain text turn into formatting after a restart and disappear | data corruption | 🔴 critical |
| 3 | One tab that fails to save blocks saving of every tab after it; errors are invisible | data loss | 🔴 critical |
| 4 | A note with a long name is never saved again | data loss | 🔴 critical |
| 5 | Writes are not atomic: a failure mid-write leaves a truncated or empty note | data loss | 🔴 critical |
| 6 | The cleared default area brings deleted text back after a restart; the area also shows up in the notes browser with a broken id | data corruption, privacy | 🔴 critical |
| 7 | Deleting an open note in the browser (`Cmd+R`) leaves its tab open: the note either comes back or silently disappears | UX, data | 🟠 high |
| 8 | Two running instances silently overwrite each other's edits | data loss | 🟠 high |
| 9 | Every 300 ms all open tabs are re-serialised on the EDT, character by character | performance | 🟠 high |
| 10 | On startup every open note is rewritten (modification dates and sort order get reset) | reliability | 🟠 high |
| 11 | The open-tabs list is saved only on exit: after a crash, the open tabs are lost | reliability | 🟠 high |
| 12 | Undo: an image resize or a zoom pushes out the whole text-editing history | UX | 🟠 high |
| 13 | If the jar sits in a folder without write access, the app silently fails to start | UX | 🟡 medium |

---

## 1. Hang on exit

**Problem.** The window closed via `EXIT_ON_CLOSE`, so `System.exit` ran directly on the EDT. The shutdown hook doing the final save called `SwingUtilities.invokeAndWait`: it waited for the EDT, while the EDT waited for the hook to finish. That is a deadlock. Reproduced with a standalone program: the process hangs forever.

**Why it matters.** The last edits (up to 300 ms of typing) and the list of open tabs are not written on close. The process keeps hanging and has to be killed.

**Fix.**
- `AppExit`: the window closes through `DO_NOTHING_ON_CLOSE` and `windowClosing`. Saving happens on the EDT, then `dispose` and `exit`. Saving runs exactly once; the hook is only for external termination (SIGTERM, logout).
- On macOS, `Cmd+Q` now goes through `QuitStrategy.CLOSE_ALL_WINDOWS`, i.e. through the same save path.
- `EdtRead` waits for the EDT for at most 2 s instead of forever. If the EDT is blocked, the read is done directly: at that point the EDT is not changing anything.
- If the final save fails, the user is asked "not saved — quit anyway?".

**Tests:** `EdtReadTest` (read while the EDT is blocked, single persist, failed save).

## 2. Markdown corrupted plain text

**Problem.** Formatting is stored as Markdown, but literal marker characters were not escaped. After a restart:
`2*3*4` → `2`*`3`*`4` (asterisks gone), `* buy milk` → italic up to the next `*`, `my__init__var` → underline.

**Why it matters.** Content is silently corrupted on every restart. Bullet lists with asterisks, formulas, code and snake_case suffer the most.

**Fix.** A new `MarkdownEscape` class:
- on write, markers are escaped with a backslash (`\*`, `\_`);
- escaping is minimal: `*` always; `_ ~ `` ` `` only when doubled or at the end of a run;
- `\` counts as special only in front of a marker, so paths like `C:\Users` and `\\server` are stored and read as is, including in old files.

**Tests:** `MarkdownEscapeTest`: 20 "dangerous" strings are round-tripped in every style, plus compatibility with old files.

> Limitation: old files where `*` is already stored unescaped are still read as formatting. New input is protected.

## 3. One error blocked all autosaving

**Problem.** In `AutoSaver.tick`, a single `try/catch` wrapped the whole loop over tabs. If saving tab N failed, tabs N+1… were never saved, and this repeated every 300 ms. The exception was simply swallowed.

**Why it matters.** The user types into notes for hours without them reaching the disk, and never finds out.

**Fix.**
- Errors are handled per tab; the other tabs still get saved.
- `SaveListener` and `SaveStatus`: on failure, the window title gets `⚠ NOT SAVED —`, and a dialog with the reason is shown once per episode. When saving recovers, the warning is cleared.
- Writes are serialised (`synchronized`), while the snapshot is taken outside the lock to avoid a deadlock with the EDT.

**Tests:** `AutoSaverReliabilityTest`, `SaveStatusTest`.

## 4. Long tab name — the note is not saved

**Problem.** The file name `note_<uuid>_<name>.txt` had no length limit. A Cyrillic name of about 105 characters already exceeds the 255-byte file-system limit, and `save` fails with "File name too long". Combined with bug #3, this meant that tab and every tab after it were not saved.

**Fix.** `NoteFileNames`: the name part of the file name is capped at 120 UTF-8 bytes without splitting surrogate pairs, and control characters are also cleaned out. The full name is still stored in the file's first line.

**Tests:** `FileTabRepositoryTest.veryLongNameIsStillSaved`, `fileNamePartIsCappedByBytes…`, `controlCharactersAreSanitized`.

## 5. Non-atomic writes

**Problem.** `Files.writeString` truncates the file first and then writes. A power loss, kill or full disk at that moment leaves an empty or truncated note. The same applied to the session, groups and options.

**Fix.** `AtomicFiles.write`: data is written to a temp file in the same folder, then moved with `ATOMIC_MOVE` and `REPLACE_EXISTING`. Used by all four stores (DRY); the duplicated `createDirectories` calls were removed.

**Tests:** `AtomicFilesTest`, `FileTabRepositoryTest.saveIsAtomic…`.

## 6. Default area: deleted text comes back, broken id

**Problem.**
- The snapshot included the default area only when it was non-empty. So clearing that area was never written, and after a restart the deleted text came back. This includes sensitive data.
- The file id was parsed at the first `_`, but `__default__` starts with one. The id came out as `""`, a "default" note showed up in `Cmd+R`, and opening it renamed the service file.

**Fix.** The default area is always part of the snapshot. The id is computed as "everything before `_<name>`" (`NoteFileNames.idOf`), which is compatible with old file names. `Tab.DEFAULT_ID` was moved into the model.

**Tests:** `NoteSessionTest.clearedScratchAreaStaysClearedAfterRestart`, `FileTabRepositoryTest.idContainingUnderscores…`, `legacyFileName…`.

## 7. Deleting an open note

**Problem.** Deleting in the browser removed the file, but the tab stayed open. If you edited it, the note came back. If you didn't, it silently disappeared on restart. On top of that, an autosaver snapshot taken before the deletion could recreate the file.

**Fix.** The lifecycle logic was moved out of `TabbedNotesUI` into `NoteSession` (SRP). `delete` closes the tab, marks the id as deleted in the autosaver (`discard`) and removes the file. Saving a closing tab also goes through the autosaver, so the EDT and the background thread no longer write the same file at the same time.

**Tests:** `NoteSessionTest` (6 scenarios), `AutoSaverReliabilityTest.discarded…`.

## 8. Two app instances

**Problem.** Double-clicking the jar twice started two windows over the same data folder. Edits in one window were silently overwritten by the other, and the session was written by whichever window closed last.

**Fix.** `InstanceLock`: `FileChannel.tryLock` on `cutenotes_data/.lock`. The second instance shows a message and exits. The OS releases the lock if the process crashes, so the folder never stays locked after a crash.

**Tests:** `InstanceLockTest`.

## 9. Typing lag on large notes

**Problem.** Every 300 ms, `snapshot()` on the EDT serialised every open tab, even unchanged ones. Serialisation went character by character: `getCharacterElement` plus `getText(i, 1)`, allocating a string for every character.

**Fix.**
- `MarkdownCache`: Markdown is recomputed only after the document changes; the cache is filled only on the EDT.
- `DocumentMarkdown.toMarkdown` walks the document by elements (runs), not by characters.

**Measurement** (a 270,000-character note): before, ~15 ms per tab every 300 ms; now ~6 ms for the changed tab only and ~0 for the rest. Previously, with 10 such tabs the EDT was busy about half the time.

**Tests:** `NoteEditorTest` (cache, invalidation on style change, serialisation with images).

## 10. All notes rewritten on startup

**Problem.** The autosaver started with an empty memory of "what is already saved", so its first tick rewrote every open note. Modification dates were reset (the browser sorts by them), and the files were exposed to risk for no reason.

**Fix.** `AutoSaver.markSaved` is called for notes loaded from disk, and new empty tabs are not written at all.

**Tests:** `NoteSessionTest.restoringTheSessionDoesNotRewriteNotes`, `AutoSaverReliabilityTest`.

## 11. Session saved only on exit

**Problem.** The list of open tabs was written only in the shutdown hook, which also hung (#1). After a crash or force quit, the set of open tabs was lost.

**Fix.** The autosaver writes the session whenever the set of non-empty tabs changes, and only then.

**Tests:** `AutoSaverReliabilityTest.sessionIsWritten…`.

## 12. Undo history

**Problem.**
- The default `UndoManager` limit is 100 edits, and every mouse move during an image resize produces 2 edits. A single resize pushed out the whole typing history, and `Cmd+Z` undid the resize one pixel at a time.
- Zoom and theme changes restyled every code character as a separate undoable edit.

**Fix.**
- `EditorUndo`: limit of 2000, and a resize is grouped into one step (`CompoundEdit`).
- `CodeRestyle`: restyling works per run and skips runs that already match, so zoom produces 0 edits.

**Tests:** `NoteEditorTest.zoomDoesNotAddUndoableEdits…`, `groupedGestureIsUndoneInOneStep…`, `colourChangeRestyles…`.

## 13. Data folder without write access

**Problem.** If the jar sat in `/Applications` or `Program Files`, `createDirectories` failed before the UI was created, and the app silently failed to start. On top of that, `DataDir.resolve()` recomputed and created the folder on every image load.

**Fix.** The folder is computed once and cached. If the folder next to the jar is not writable, `~/cutenotes_data` is used instead.

**Tests:** `DataDirTest`.

---

## Found but not fixed (recommendations)

- **Image scaling** via `getScaledInstance(SCALE_SMOOTH)` takes ~19 ms per step for a retina screenshot. This is noticeable during resize and when opening a note with many images. Scaling through `Graphics2D` is better (~0.7 ms; for quality, with progressive downscaling).
- **Images are never removed from `cutenotes_data/images`** after being deleted from a note, so the folder grows forever. It needs garbage collection based on references from all notes.
- **Image paths from Markdown are not confined to the `images` folder** (`![](../../x.png)`). "Open full image" will open an arbitrary image file. The path should be normalised and checked to be inside the data folder.
- **Old files** with unescaped `*`/`__` are displayed with formatting (see #2): an intentional marker cannot be told apart from an accidental one.

## New classes (SRP)

`dao`: `AtomicFiles`, `NoteFileNames`, `InstanceLock` · `markdown`: `MarkdownEscape` · `util`: `SaveListener` · `view`: `NoteSession`, `AppExit`, `SaveStatus`, `EdtRead` (rewritten), `MarkdownCache`, `EditorUndo`, `CodeRestyle`, `TabCards`. The `TabCleanup` class was removed: its logic now lives in `Tab.inSession()` and `NoteSession`.
