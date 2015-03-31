import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.me.viv.logmyride.Fence;
import uk.me.viv.logmyride.GeoFence;

public class GeoFenceTest {

    private static GeoFence geoFence;

    public GeoFenceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        Fence fence = new Fence("51.545525", "51.539827", "-3.57891", "-3.568107");
        geoFence = new GeoFence(fence);
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
//       this.geoFence.fence();
    }
}