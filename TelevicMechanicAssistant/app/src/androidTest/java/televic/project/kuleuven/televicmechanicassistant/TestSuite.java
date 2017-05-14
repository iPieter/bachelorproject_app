package televic.project.kuleuven.televicmechanicassistant;

/**
 * Created by Matthias on 13/05/2017.
 */

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import televic.project.kuleuven.televicmechanicassistant.data.ContentProviderTest;
import televic.project.kuleuven.televicmechanicassistant.data.UriMatcherTest;

// TestSuite to run all unit tests
@RunWith(Suite.class)
@Suite.SuiteClasses({UriMatcherTest.class,
        ContentProviderTest.class
})
public class TestSuite {}
