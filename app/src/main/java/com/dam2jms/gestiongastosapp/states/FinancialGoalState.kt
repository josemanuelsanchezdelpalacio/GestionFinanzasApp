package com.dam2jms.gestiongastosapp.states

import java.time.LocalDate
import java.util.UUID

// State for Financial Goals
data class FinancialGoalState(
    val id: String? = UUID.randomUUID().toString(),
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val startDate: String = LocalDate.now().toString(),
    val endDate: String = LocalDate.now().plusMonths(6).toString(),
    val goalName: String = "",
    val goalCategory: String = "",
    val progress: Double = 0.0
) {
    // Calculate progress percentage
    fun calculateProgressPercentage(): Double {
        return if (targetAmount > 0) (currentAmount / targetAmount) * 100 else 0.0
    }

    // Determine if goal is achieved
    fun isGoalAchieved(): Boolean {
        return currentAmount >= targetAmount
    }

    // Estimate days remaining to achieve goal
    fun estimateDaysRemaining(): Int {
        val startLocalDate = LocalDate.parse(startDate)
        val endLocalDate = LocalDate.parse(endDate)
        return startLocalDate.until(endLocalDate).days
    }
}

// State for Budget Tracking
data class BudgetState(
    val id: String? = UUID.randomUUID().toString(),
    val category: String = "",
    val budgetAmount: Double = 0.0,
    val currentSpent: Double = 0.0,
    val startDate: String = LocalDate.now().toString(),
    val endDate: String = LocalDate.now().plusMonths(1).toString(),
    val remainingAmount: Double = budgetAmount
) {
    // Calculate budget usage percentage
    fun calculateBudgetUsagePercentage(): Double {
        return if (budgetAmount > 0) (currentSpent / budgetAmount) * 100 else 0.0
    }

    // Check if budget is overspent
    fun isOverBudget(): Boolean {
        return currentSpent > budgetAmount
    }

    // Estimate remaining budget percentage
    fun remainingBudgetPercentage(): Double {
        return 100 - calculateBudgetUsagePercentage()
    }
}


