package xyz.vopen.framework.cropdb.transaction;

/**
 * Represents a change type in a transaction.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
enum ChangeType {
  /** Insert */
  Insert,

  /** Update. */
  Update,

  /** Remove. */
  Remove,

  /** Clear. */
  Clear,

  /** Create index. */
  CreateIndex,

  /** Rebuild index. */
  RebuildIndex,

  /** Drop index. */
  DropIndex,

  /** Drop all indices. */
  DropAllIndices,

  /** Drop collection. */
  DropCollection,

  /** Set attribute. */
  SetAttribute,

  /** Add processor */
  AddProcessor,

  /** Remove processor */
  RemoveProcessor,
}
