package tests.store;

import org.junit.Before;
import org.junit.Test;
import store.Membership;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MembershipTest
{
    // Static variables that will be used during testing
    final String membership1ID = "1";
    final String membership2ID = "2";
    final String membership1Name = "Customer 1";
    final String membership2Name = "Customer 2";

    // Setup that is run before each test case
    @Before
    public void setup()
    {
        // Resets the membership information
        Membership.clear();
    }

    @Test
    public void membershipSignupAndCheckTest()
    {
        assertFalse(Membership.isMember(membership1ID));
        assertFalse(Membership.isMember(membership2ID));

        Membership.createMembership(membership1ID, membership1Name);
        Membership.createMembership(membership2ID, membership2Name);

        assertTrue(Membership.isMember(membership1ID));
        assertTrue(Membership.isMember(membership2ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void membershipDuplicationSignupTest()
    {
        Membership.createMembership(membership1ID, membership1Name);
        Membership.createMembership(membership1ID, membership2Name);
    }

    @Test
    public void membershipClearTest()
    {
        assertFalse(Membership.isMember(membership1ID));
        assertFalse(Membership.isMember(membership2ID));

        Membership.createMembership(membership1ID, membership1Name);
        Membership.createMembership(membership2ID, membership2Name);
        Membership.clear();

        assertFalse(Membership.isMember(membership1ID));
        assertFalse(Membership.isMember(membership2ID));
    }
}
