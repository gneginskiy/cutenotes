package model;

import java.time.Instant;

public record TabMeta(String id, String name, Instant lastModified) {}
