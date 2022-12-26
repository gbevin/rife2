/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionvalidators;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.authentication.SessionAttributes;
import rife.authentication.SessionValidator;
import rife.authentication.credentialsmanagers.DatabaseUsers;
import rife.authentication.credentialsmanagers.DatabaseUsersFactory;
import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.exceptions.CredentialsManagerException;
import rife.authentication.exceptions.SessionManagerException;
import rife.authentication.exceptions.SessionValidatorException;
import rife.authentication.sessionmanagers.DatabaseSessions;
import rife.authentication.sessionmanagers.DatabaseSessionsFactory;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.tools.ExceptionUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestDatabaseSessionValidator {
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testInstantiation(Datasource datasource) {
        SessionValidator validator = DatabaseSessionValidatorFactory.instance(datasource);
        assertNotNull(validator);
        assertTrue(validator instanceof DatabaseSessionValidator);
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testValidSessionId(Datasource datasource) {
        SessionValidator validator = DatabaseSessionValidatorFactory.instance(datasource);

        assertTrue(validator.isAccessAuthorized(1));
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testSessionValidity(Datasource datasource) {
        SessionValidator validator = DatabaseSessionValidatorFactory.instance(datasource);
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);
        sessions.setSessionDuration(120000);
        validator.setSessionManager(sessions);

        int user_id = 9478;
        String host_ip = "98.232.12.456";

        String auth_id = null;
        try {
            sessions.install();

            auth_id = sessions.startSession(user_id, host_ip, false);
            assertTrue(validator.isAccessAuthorized(validator.validateSession(auth_id, host_ip, new DummyAttributes())));
            sessions.setRestrictHostIp(true);
            assertEquals(DatabaseSessionValidator.SESSION_INVALID, validator.validateSession(auth_id, "1.1.1.1", new DummyAttributes()));
            sessions.setRestrictHostIp(false);
            assertEquals(DatabaseSessionValidator.SESSION_VALID, validator.validateSession(auth_id, "1.1.1.1", new DummyAttributes()));
            assertEquals(DatabaseSessionValidator.SESSION_INVALID, validator.validateSession("not_valid", host_ip, new DummyAttributes()));

            sessions.setSessionDuration(0);

            Thread.sleep(2);
            assertEquals(DatabaseSessionValidator.SESSION_INVALID, validator.validateSession(auth_id, host_ip, new DummyAttributes()));
        } catch (InterruptedException | SessionValidatorException | SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testSessionValidityRole(Datasource datasource) {
        SessionValidator validator = DatabaseSessionValidatorFactory.instance(datasource);
        DatabaseUsers users = DatabaseUsersFactory.instance(datasource);
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);
        sessions.setSessionDuration(120000);
        validator.setSessionManager(sessions);

        String host_ip = "98.232.12.456";

        String auth_id1 = null;
        String auth_id2 = null;
        String auth_id3 = null;
        try {
            users.install();
            sessions.install();

            users.addRole("admin");
            users.addRole("maint");

            users.addUser("login1", new RoleUserAttributes(1, "thepassword", new String[]{"admin", "maint"}));
            users.addUser("login2", new RoleUserAttributes(2, "thepassword", new String[]{"maint"}));
            users.addUser("login3", new RoleUserAttributes(3, "thepassword"));

            auth_id1 = sessions.startSession(1, host_ip, false);
            auth_id2 = sessions.startSession(2, host_ip, false);
            auth_id3 = sessions.startSession(3, host_ip, false);

            assertTrue(validator.isAccessAuthorized(validator.validateSession(auth_id1, host_ip, new DummyAttributes())));
            assertTrue(validator.isAccessAuthorized(validator.validateSession(auth_id1, host_ip, new RoleAdminAttributes())));
            assertTrue(validator.isAccessAuthorized(validator.validateSession(auth_id1, host_ip, new RoleMaintAttributes())));

            assertTrue(validator.isAccessAuthorized(validator.validateSession(auth_id2, host_ip, new DummyAttributes())));
            assertEquals(AbstractSessionValidator.SESSION_INVALID, validator.validateSession(auth_id2, host_ip, new RoleAdminAttributes()));
            assertTrue(validator.isAccessAuthorized(validator.validateSession(auth_id2, host_ip, new RoleMaintAttributes())));

            assertTrue(validator.isAccessAuthorized(validator.validateSession(auth_id3, host_ip, new DummyAttributes())));
            assertEquals(AbstractSessionValidator.SESSION_INVALID, validator.validateSession(auth_id3, host_ip, new RoleAdminAttributes()));
            assertEquals(AbstractSessionValidator.SESSION_INVALID, validator.validateSession(auth_id3, host_ip, new RoleMaintAttributes()));
        } catch (SessionManagerException | SessionValidatorException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } catch (CredentialsManagerException e) {
            e.printStackTrace();
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    static class DummyAttributes implements SessionAttributes {
        public boolean hasAttribute(String key) {
            return false;
        }

        public String getAttribute(String key) {
            return null;
        }
    }

    static class RoleMaintAttributes implements SessionAttributes {
        public boolean hasAttribute(String key) {
            return key.equals("role");
        }

        public String getAttribute(String key) {
            if (key.equals("role")) {
                return "maint";
            }

            return null;
        }
    }

    static class RoleAdminAttributes implements SessionAttributes {
        public boolean hasAttribute(String key) {
            return key.equals("role");
        }

        public String getAttribute(String key) {
            if (key.equals("role")) {
                return "admin";
            }

            return null;
        }
    }
}
