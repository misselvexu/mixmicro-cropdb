package xyz.vopen.framework.cropdb.transaction;

import lombok.extern.slf4j.Slf4j;
import xyz.vopen.framework.cropdb.CropConfig;
import xyz.vopen.framework.cropdb.exceptions.IndexingException;
import xyz.vopen.framework.cropdb.index.CropIndexer;
import xyz.vopen.framework.cropdb.common.mapper.CropMapper;
import xyz.vopen.framework.cropdb.common.module.CropModule;
import xyz.vopen.framework.cropdb.store.CropStore;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@Slf4j
class TransactionConfig extends CropConfig {
  private final CropConfig config;

  public TransactionConfig(CropConfig config) {
    super();
    this.config = config;
  }

  @Override
  public CropIndexer findIndexer(String indexType) {
    CropIndexer cropIndexer = pluginManager.getIndexerMap().get(indexType);
    if (cropIndexer != null) {
      cropIndexer.initialize(this);
      return cropIndexer;
    } else {
      throw new IndexingException("no indexer found for index type " + indexType);
    }
  }

  @Override
  public void fieldSeparator(String separator) {
    config.fieldSeparator(separator);
  }

  @Override
  public CropConfig loadModule(CropModule module) {
    pluginManager.loadModule(module);
    return this;
  }

  @Override
  public void autoConfigure() {
    pluginManager.findAndLoadPlugins();
  }

  @Override
  public CropMapper cropMapper() {
    return config.cropMapper();
  }

  @Override
  public CropStore<?> getCropStore() {
    return pluginManager.getCropStore();
  }

  @Override
  public void initialize() {
    super.initialize();
  }
}
