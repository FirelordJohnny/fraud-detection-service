package com.faud.frauddetection.mapper;

import com.faud.frauddetection.entity.FraudRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MyBatis Mapper for FraudRule.
 * All SQL queries are defined in 'resources/mapper/FraudRuleMapper.xml'.
 */
@Mapper
public interface FraudRuleMapper {

    /**
     * Inserts a new fraud rule into the database.
     *
     * @param fraudRule The fraud rule to insert.
     */
    void insert(FraudRule fraudRule);

    /**
     * Finds a fraud rule by its ID.
     *
     * @param id The ID of the fraud rule.
     * @return An Optional containing the fraud rule if found.
     */
    Optional<FraudRule> findById(Long id);

    /**
     * Finds all fraud rules.
     *
     * @return A list of all fraud rules.
     */
    List<FraudRule> findAll();

    /**
     * Finds all active (enabled) fraud rules.
     *
     * @return A list of active fraud rules.
     */
    List<FraudRule> findAllActive();

    /**
     * Updates an existing fraud rule.
     *
     * @param fraudRule The fraud rule with updated information.
     * @return The number of rows affected.
     */
    int update(FraudRule fraudRule);

    /**
     * Deletes a fraud rule by its ID.
     *
     * @param id The ID of the fraud rule to delete.
     * @return The number of rows affected.
     */
    int delete(Long id);

    /**
     * Updates the status (enabled/disabled) of a fraud rule.
     *
     * @param id        The ID of the fraud rule.
     * @param enabled   The new status.
     * @param updatedAt The timestamp of the update.
     * @return The number of rows affected.
     */
    int updateStatus(@Param("id") Long id, @Param("enabled") boolean enabled, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Finds all enabled fraud rules of a specific type.
     *
     * @param ruleType The type of the rule.
     * @return A list of matching enabled fraud rules.
     */
    List<FraudRule> findByRuleType(String ruleType);

    /**
     * Finds all enabled fraud rules.
     *
     * @return A list of all enabled fraud rules.
     */
    List<FraudRule> findAllEnabled();
} 