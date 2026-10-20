package com.example.fasopronostics.data.model

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val priceFCFA: Int,
    val durationDays: Int,
    val description: String,
    val badge: String? = null,
    val isPopular: Boolean = false,
    val features: List<String> = emptyList()
)

data class UserSubscription(
    val isPremium: Boolean,
    val planName: String?,
    val expiryTimestamp: Long?,
    val daysRemaining: Int = 0
)

data class ActivationCode(
    val code: String,
    val planId: String,
    val planName: String,
    val durationDays: Int,
    val isUsed: Boolean = false,
    val isRevoked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val usedAt: Long? = null,
    val usedBy: String? = null
)

data class PaymentChannel(
    val id: String,
    val name: String,
    val dialCode: String,
    val instructions: String,
    val logoText: String
)
