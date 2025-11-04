package edu.bu.data;

import edu.bu.analytics.UnknownBuoyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryStoreTest {
    
    private InMemoryStore store;
    
    @BeforeEach
    public void setUp() {
        store = new InMemoryStore();
    }
    
    /* Update the store with two measurements for the same buoy */
    @Test
    public void testUpdateAndRetrieve() throws UnknownBuoyException {
        // Create test data
        BuoyResponse response1 = new BuoyResponse("temperature", 20.5, 1, System.currentTimeMillis());
        BuoyResponse response2 = new BuoyResponse("salinity", 35.0, 1, System.currentTimeMillis());
        
        // Update store
        store.update(Arrays.asList(response1, response2));
        
        // Retrieve and verify
        List<BuoyResponse> history = store.getHistory(1);
        assertEquals(2, history.size());
        assertEquals(20.5, history.get(0).measurementVal);
        assertEquals(35.0, history.get(1).measurementVal);
    }
    
    /* Update two buoys */
    @Test
    public void testMultipleBuoys() throws UnknownBuoyException {
        BuoyResponse buoy1Response = new BuoyResponse("temperature", 20.5, 1, System.currentTimeMillis());
        BuoyResponse buoy2Response = new BuoyResponse("temperature", 22.0, 2, System.currentTimeMillis());
        
        store.update(Arrays.asList(buoy1Response, buoy2Response));
        
        assertEquals(1, store.getHistory(1).size());
        assertEquals(1, store.getHistory(2).size());
        assertEquals(20.5, store.getHistory(1).get(0).measurementVal);
        assertEquals(22.0, store.getHistory(2).get(0).measurementVal);
    }
    
    /* Update nonexisting buoy */
    @Test
    public void testUnknownBuoyThrowsException() {
        assertThrows(UnknownBuoyException.class, () -> {
            store.getHistory(999);
        });
    }
    
    @Test
    public void testNullResponseHandling() throws UnknownBuoyException {
        BuoyResponse response1 = new BuoyResponse("temperature", 20.5, 1, System.currentTimeMillis());
        
        store.update(Arrays.asList(response1, null));
        
        List<BuoyResponse> history = store.getHistory(1);
        assertEquals(1, history.size()); // null should be skipped
    }
    
    @Test
    public void testReturnedListIsIndependent() throws UnknownBuoyException {
        BuoyResponse response = new BuoyResponse("temperature", 20.5, 1, System.currentTimeMillis());
        store.update(Arrays.asList(response));
        
        List<BuoyResponse> history1 = store.getHistory(1);
        List<BuoyResponse> history2 = store.getHistory(1);
        
        assertNotSame(history1, history2);
    }
}