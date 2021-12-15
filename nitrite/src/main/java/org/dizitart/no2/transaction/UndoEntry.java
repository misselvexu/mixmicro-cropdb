package org.dizitart.no2.transaction;

import lombok.Data;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@Data
class UndoEntry {
    private String collectionName;
    private Command rollback;
}
