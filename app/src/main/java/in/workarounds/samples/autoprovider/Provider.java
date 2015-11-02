package in.workarounds.samples.autoprovider;

import in.workarounds.autoprovider.AutoProvider;

/**
 * Created by mouli on 10/29/15.
 */
@AutoProvider(authority = "in.workarounds.sample.autoprovider.provider",
        providerName = "SampleProvider",
        packageName = "in.workarounds.samples.autoprovider",
        databaseFileName = "sample.db",
        databaseVersion = 1)
public class Provider {
}
