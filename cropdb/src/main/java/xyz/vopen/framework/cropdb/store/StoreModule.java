package xyz.vopen.framework.cropdb.store;

import xyz.vopen.framework.cropdb.common.module.CropModule;

/**
 * Represents a crop store module to load as a storage engine for the database.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public interface StoreModule extends CropModule {
  /**
   * Gets the {@link CropStore} instance from this module.
   *
   * @return the store
   */
  CropStore<?> getStore();
}
