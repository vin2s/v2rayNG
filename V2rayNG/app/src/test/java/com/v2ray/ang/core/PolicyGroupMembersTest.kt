package com.v2ray.ang.core

import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.enums.EConfigType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PolicyGroupMembersTest {

    @Test
    fun selfUsesTheGroupSubscription() {
        val group = ProfileItem.create(EConfigType.POLICYGROUP).apply {
            subscriptionId = "sub-a"
            policyGroupSubscriptionId = "self"
        }
        assertEquals("sub-a", policyGroupSourceSubscriptionId(group))
        assertTrue(
            isPolicyGroupMember(
                group,
                ProfileItem.create(EConfigType.VLESS).apply { subscriptionId = "sub-a" },
            )
        )
        assertFalse(
            isPolicyGroupMember(
                group,
                ProfileItem.create(EConfigType.VLESS).apply { subscriptionId = "sub-b" },
            )
        )
    }

    @Test
    fun blankSubscriptionIncludesAllSimpleProfiles() {
        val group = ProfileItem.create(EConfigType.POLICYGROUP)
        assertNull(policyGroupSourceSubscriptionId(group))
        assertTrue(isPolicyGroupMember(group, ProfileItem.create(EConfigType.TROJAN)))
        assertFalse(isPolicyGroupMember(group, ProfileItem.create(EConfigType.POLICYGROUP)))
    }
}
