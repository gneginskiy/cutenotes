# cuteNotes

A minimalistic cross-platform (Linux, Windows, macOS) always-on-top scratchpad with auto-saved tabs, written in Java.
Stores everything in plain text.

## How it works
A `cutenotes_data/` folder is created next to the jar. It holds your notes, session, and options.

## Run
Install Java 21+, download the release jar, double-click on it.

## Download 
[cutenotes-2026-05-15.jar](releases/cutenotes-2026-05-15.jar) - latest release (tabs, theming, redo, etc.)


## Shortcuts

Press **F1** in the app for the full, OS-aware list. Highlights:

| Action                      | macOS | Win / Linux |
|-----------------------------| --- | --- |
| New / Close tab             | `Cmd+T` / `Cmd+W` | `Ctrl+T` / `Ctrl+W` |
| Reopen last closed / picker | `Cmd+Shift+T` / `Cmd+R` | `Ctrl+Shift+T` / `Ctrl+R` |
| Next tab                    | `Option+Tab` or `Ctrl+Tab` | `Ctrl+Tab` |
| Find                        | `Cmd+F` | `Ctrl+F` |
| Options                     | `Cmd+O` | `Ctrl+O` |
| Zoom in / out text          | `Cmd+Shift+=` / `Cmd+Shift+-` | `Ctrl+Shift+=` / `Ctrl+Shift+-` |
| Undo / Redo                 | `Cmd+Z` / `Cmd+Shift+Z` | `Ctrl+Z` / `Ctrl+Shift+Z` |
| Cut / copy / duplicate line | `Cmd+X` / `Cmd+C` / `Cmd+D` | `Ctrl+X` / `Ctrl+C` / `Ctrl+D` |
| Toggle status bar / Help    | `Esc` / `F1` | `Esc` / `F1` |

## Build
```
mvn clean install
```

Output: `target/cutenotes.jar`.

