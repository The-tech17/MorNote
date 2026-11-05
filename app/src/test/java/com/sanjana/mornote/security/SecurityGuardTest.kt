package com.sanjana.mornote.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SecurityGuardTest {

    @Test
    fun testSecurityPolicyDefaultsAndConfiguration() {
        assertNotNull("SecurityGuard object must be accessible", SecurityGuard)
        assertEquals("Default active policy must be BLOCK_ENTRY", SecurityGuard.SecurityPolicy.BLOCK_ENTRY, SecurityGuard.activePolicy)

        SecurityGuard.activePolicy = SecurityGuard.SecurityPolicy.EMERGENCY_WIPE
        assertEquals("Active policy should update to EMERGENCY_WIPE", SecurityGuard.SecurityPolicy.EMERGENCY_WIPE, SecurityGuard.activePolicy)

        SecurityGuard.activePolicy = SecurityGuard.SecurityPolicy.BLOCK_ENTRY
    }
}
