package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dao.FileTabRepository;
import model.Tab;
import model.TabMeta;
import util.AutoSaver;

class ExternalEditsTest {

  static {
    System.setProperty("java.awt.headless", "true");
    Messages.useLanguage("en");
  }

  @TempDir Path data;
  private final List<String> toasts = new ArrayList<>();
  private TabsPane tabs;
  private AutoSaver saver;
  private ExternalEdits edits;
  private FileTabRepository repo;

  private void open(String text) throws Exception {
    repo = new FileTabRepository(data);
    repo.save("a", "Alpha", text);
    saver = new AutoSaver(List::of, repo);
    SwingUtilities.invokeAndWait(
        () -> {
          tabs = new TabsPane();
          tabs.openTab("a", "Alpha", text);
          saver.markSaved(tabs.snapshotOf("a"));
        });
    edits = new ExternalEdits(repo, tabs, saver, toasts::add);
    edits.check();
  }

  private void changeOnDisk(String text) throws Exception {
    repo.save("a", "Alpha", text);
    touch();
    edits.check();
    SwingUtilities.invokeAndWait(() -> {});
  }

  /** Moves the note's file time on, as any write does. */
  private void touch() throws Exception {
    try (Stream<Path> files = Files.list(data)) {
      Path file = files.filter(p -> p.getFileName().toString().contains("a")).findFirst().get();
      Files.setLastModifiedTime(file, FileTime.from(Instant.now().plusSeconds(60)));
    }
  }

  @Test
  void withoutUnsavedEditsTheTabShowsTheNewText() throws Exception {
    open("old");

    changeOnDisk("from sync");

    SwingUtilities.invokeAndWait(() -> assertEquals("from sync", tabs.areaOf("a").markdown()));
    assertEquals("from sync", saver.lastSaved("a").content());
    assertEquals(1, toasts.size());
  }

  @Test
  void withUnsavedEditsBothVersionsAreKept() throws Exception {
    open("old");
    SwingUtilities.invokeAndWait(() -> tabs.areaOf("a").setMarkdown("my edit"));

    changeOnDisk("from sync");

    SwingUtilities.invokeAndWait(() -> assertEquals("my edit", tabs.areaOf("a").markdown()));
    List<TabMeta> notes = repo.listMeta();
    assertEquals(2, notes.size());
    TabMeta copy = notes.stream().filter(m -> !m.id().equals("a")).findFirst().get();
    assertEquals("Alpha (from disk)", copy.name());
    assertEquals("from sync", repo.load(copy.id()).content());
  }

  /**
   * The app's own autosave may land between reading the file and asking what was saved last. The
   * file then looks older than the app's last save and the tab used to be "reloaded" with it,
   * throwing the latest typing away.
   */
  @Test
  void theAppsOwnSaveDuringACheckIsNotTakenForAnOutsideChange() throws Exception {
    open("typed 1");
    SwingUtilities.invokeAndWait(() -> tabs.areaOf("a").setMarkdown("typed 1, typed 2"));
    Tab typed = new Tab("a", "Alpha", "typed 1, typed 2");
    FileTabRepository racing =
        new FileTabRepository(data) {
          private boolean raced;

          @Override
          public Tab load(String id) {
            Tab onDisk = super.load(id);
            if (!raced) {
              raced = true;
              Thread tick = Thread.ofVirtual().start(() -> saver.save(typed));
              try {
                tick.join(300);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            }
            return onDisk;
          }
        };
    edits = new ExternalEdits(racing, tabs, saver, toasts::add);
    edits.check();
    touch();

    edits.check();
    SwingUtilities.invokeAndWait(() -> {});
    Thread.sleep(100);
    SwingUtilities.invokeAndWait(() -> {});

    SwingUtilities.invokeAndWait(
        () -> assertEquals("typed 1, typed 2", tabs.areaOf("a").markdown()));
    assertEquals(1, repo.listMeta().size(), "no copy of the app's own text");
    assertTrue(toasts.isEmpty(), toasts.toString());
  }

  @Test
  void closedTabsAreLeftAlone() throws Exception {
    open("old");
    SwingUtilities.invokeAndWait(
        () -> {
          tabs.close("a");
          edits.apply(new Tab("a", "Alpha", "new"), new Tab("a", "Alpha", "old"));
        });

    assertTrue(toasts.isEmpty());
  }
}
