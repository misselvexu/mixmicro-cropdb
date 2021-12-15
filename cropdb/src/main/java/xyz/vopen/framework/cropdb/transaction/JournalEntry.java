package xyz.vopen.framework.cropdb.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a transaction journal entry.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
class JournalEntry {
    private ChangeType changeType;
    private Command commit;
    private Command rollback;
}
