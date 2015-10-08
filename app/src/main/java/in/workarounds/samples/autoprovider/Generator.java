package in.workarounds.samples.autoprovider;

import in.workarounds.autoprovider.AutoProvider;

/**
 * Created by madki on 08/10/15.
 */
@AutoProvider(
        authority = "in.workarounds.samples.autoprovider.authority",
        databaseName = "sample.db",
        databaseVersion = 1
)
public final class Generator {
}
