package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class SystemDarkModeTest {

  @Test
  void readsMacOs() {
    assertEquals("defaults", SystemDarkMode.command("Mac OS X").get(0));
    assertEquals(Optional.of(true), SystemDarkMode.parse("Mac OS X", 0, "Dark\n"));
    assertEquals(Optional.of(false), SystemDarkMode.parse("Mac OS X", 1, "does not exist"));
  }

  @Test
  void readsWindows() {
    assertEquals("reg", SystemDarkMode.command("Windows 11").get(0));
    String dark = "    AppsUseLightTheme    REG_DWORD    0x0";
    assertEquals(Optional.of(true), SystemDarkMode.parse("Windows 11", 0, dark));
    assertEquals(
        Optional.of(false), SystemDarkMode.parse("Windows 11", 0, dark.replace("0x0", "0x1")));
    assertEquals(Optional.empty(), SystemDarkMode.parse("Windows 11", 1, "error"));
  }

  @Test
  void readsLinuxDesktops() {
    assertEquals("gsettings", SystemDarkMode.command("Linux").get(0));
    assertEquals(Optional.of(true), SystemDarkMode.parse("Linux", 0, "'prefer-dark'"));
    assertEquals(Optional.of(false), SystemDarkMode.parse("Linux", 0, "'default'"));
    assertEquals(Optional.of(false), SystemDarkMode.parse("Linux", 0, "'prefer-light'"));
    assertEquals(Optional.empty(), SystemDarkMode.parse("Linux", 0, "???"));
  }

  @Test
  void asksTheRunningSystemWithoutFailing() {
    assertNotNull(SystemDarkMode.detect());
  }
}
