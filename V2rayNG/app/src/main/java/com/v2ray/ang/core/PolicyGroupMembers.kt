package com.v2ray.ang.core

import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.extension.isComplexType

internal const val POLICY_GROUP_SELF_SUBSCRIPTION = "self"

internal fun policyGroupSourceSubscriptionId(group: ProfileItem): String? {
    val raw = group.policyGroupSubscriptionId?.trim().orEmpty()
    return when {
        raw.isEmpty() -> null
        raw.equals(POLICY_GROUP_SELF_SUBSCRIPTION, ignoreCase = true) ->
            group.subscriptionId.trim().takeIf { it.isNotEmpty() }
        else -> raw
    }
}

internal fun isPolicyGroupMember(group: ProfileItem, candidate: ProfileItem): Boolean {
    if (candidate.configType.isComplexType()) return false
    val sourceSubscriptionId = policyGroupSourceSubscriptionId(group)
    if (!sourceSubscriptionId.isNullOrEmpty() && candidate.subscriptionId != sourceSubscriptionId) {
        return false
    }
    return true
}
