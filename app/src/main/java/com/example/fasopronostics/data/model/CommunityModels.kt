package com.example.fasopronostics.data.model

data class ReferralProfile(
    val referralCode: String,
    val totalInvited: Int,
    val completedReferrals: Int,
    val targetForReward: Int = 3,
    val rewardDaysGranted: Int = 0
)

data class CommunityPoll(
    val id: String,
    val matchTitle: String,
    val question: String,
    val optionHome: String,
    val votesHome: Int,
    val optionDraw: String,
    val votesDraw: Int,
    val optionAway: String,
    val votesAway: Int,
    val userVotedOption: Int? = null // 1, 2, or 3
)
