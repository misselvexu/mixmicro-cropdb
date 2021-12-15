package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;

/**
 * A command to drop a nitrite collection.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
@AllArgsConstructor
public class Drop extends BaseCommand implements Command {
    private final String collectionName;

    @Override
    public void execute(Nitrite nitrite) {
        initialize(nitrite, collectionName);
        operations.dropCollection();
    }
}
