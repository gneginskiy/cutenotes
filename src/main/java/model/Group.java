package model;

/** A note group node. {@code parentId} is {@code null} for a top-level group. */
public record Group(String id, String name, String parentId) {}
