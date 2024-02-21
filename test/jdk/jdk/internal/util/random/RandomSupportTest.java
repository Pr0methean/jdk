/*
 * @test
 * @bug 8326227
 * @modules java.base/jdk.internal.util.random
 * @summary Verify that RandomSupport methods behave as specified
 * @run junit RandomSupportTest
 */

import org.junit.jupiter.api.*;
import java.util.random.RandomGenerator;
import jdk.internal.util.random.RandomSupport;
import static org.junit.jupiter.api.Assertions.*;
public class RandomSupportTest {
    private static class WorstCaseRandomGenerator implements RandomGenerator {
        boolean havePreviousOutput = false;
        @Override
        public long nextLong() {
            if (havePreviousOutput) {
                // gets shifted to 0x4000_0000_0000_0000, which puts us in the center of the sampling rectangle and
                // ensures that we'll never choose a point under the curve
                return Long.MIN_VALUE;
            } else {
                havePreviousOutput = true;
                return Long.MIN_VALUE | 255; // need high value in last byte in order to skip the fast path
            }
        }
    }
    @Test
    public void testNextExponentialSoftCapped() {
        for (long maxExp = 0; maxExp < 65; max++) {
            double max = Math.scalb(1.0, maxExp);
            WorstCaseRandomGenerator rng = new WorstCaseRandomGenerator();
            assertTrue(RandomSupport.computeNextExponentialSoftCapped(rng, max) >= max);
        }
    }
}