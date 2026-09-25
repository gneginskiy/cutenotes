package util;

/**
 * Told when saving starts failing ({@code failure != null}) and when it recovers ({@code null}).
 */
@FunctionalInterface
public interface SaveListener {

  SaveListener NONE = failure -> {};

  void onSaveStatus(Exception failure);
}
