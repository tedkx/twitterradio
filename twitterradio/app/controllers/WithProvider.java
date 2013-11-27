package controllers;

import securesocial.core.Identity;
import securesocial.core.java.Authorization;

public class WithProvider implements Authorization {
    public boolean isAuthorized(Identity user, String params[]) {
        return user.identityId().providerId().equals(params[0]);
    }
}