package xyz.vopen.framework.cropdb.rocksdb;

import lombok.extern.slf4j.Slf4j;
import xyz.vopen.framework.cropdb.exceptions.InvalidOperationException;
import xyz.vopen.framework.cropdb.common.util.StringUtils;

import static xyz.vopen.framework.cropdb.common.util.StringUtils.isNullOrEmpty;

@Slf4j
class RocksDBStoreUtils {
  private RocksDBStoreUtils() {}

  public static RocksDBReference openOrCreate(RocksDBConfig storeConfig) {
    RocksDBReference db;
    if (!StringUtils.isNullOrEmpty(storeConfig.filePath())) {
      db = StoreFactory.createDBReference(storeConfig);
    } else {
      throw new InvalidOperationException("crop rocksdb store does not support in-memory database");
    }
    return db;
  }
}
