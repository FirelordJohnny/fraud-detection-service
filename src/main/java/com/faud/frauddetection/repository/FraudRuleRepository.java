package com.faud.frauddetection.repository;

import com.faud.frauddetection.entity.FraudRule;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing FraudRule entities.
 * This acts as a Data Access Layer (DAL) to abstract the underlying data source.
 */
public interface FraudRuleRepository {

    /**
     * Finds a fraud rule by its ID.
     * @param id The ID of the rule.
     * @return An optional containing the rule if found.
     */
    Optional<FraudRule> findById(Long id);

    /**
     * Finds all fraud rules.
     * @return A list of all rules.
     */
    List<FraudRule> findAll();

    /**
     * Finds all enabled (active) fraud rules.
     * @return A list of all enabled rules.
     */
    List<FraudRule> findAllEnabled();

    /**
     * Saves a new or updated fraud rule.
     * @param fraudRule The rule to save.
     */
    void save(FraudRule fraudRule);

    /**
     * Updates a fraud rule.
     * @param fraudRule The rule to update.
     */
    void update(FraudRule fraudRule);

    /**
     * Deletes a fraud rule by its ID.
     * @param id The ID of the rule to delete.
     */
    void delete(Long id);
} 