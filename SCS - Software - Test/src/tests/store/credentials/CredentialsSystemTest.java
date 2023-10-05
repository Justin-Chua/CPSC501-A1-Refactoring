package tests.store.credentials;

import org.junit.Test;
import store.credentials.CredentialsSystem;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CredentialsSystemTest
{
    // Static variables that will be used during testing
    final String username1 = "username1";
    final String username2 = "username2";
    final String password1 = "password1";
    final String password2 = "password2";

    @Test
    public void credentialsSystemAddAndCheckAndRemoveTest()
    {
        assertFalse(CredentialsSystem.checkLogin(username1, password1));
        assertFalse(CredentialsSystem.checkLogin(username2, password2));

        CredentialsSystem.addAccount(username1, password1);
        CredentialsSystem.addAccount(username2, password2);

        assertTrue(CredentialsSystem.checkLogin(username1, password1));
        assertTrue(CredentialsSystem.checkLogin(username2, password2));
        assertFalse(CredentialsSystem.checkLogin(username1, password2));
        assertFalse(CredentialsSystem.checkLogin(username2, password1));

        CredentialsSystem.removeAccount(username1);
        CredentialsSystem.removeAccount(username2);

        assertFalse(CredentialsSystem.checkLogin(username1, password1));
        assertFalse(CredentialsSystem.checkLogin(username2, password2));
    }
}
