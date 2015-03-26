import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.me.viv.logmyride.GeoFence;

public class GeoFenceTest {

    private static GeoFence geoFence;

    public GeoFenceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        geoFence = new GeoFence();
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void canCheckStartCoordinates() {
       this.geoFence.fence();
    }
}