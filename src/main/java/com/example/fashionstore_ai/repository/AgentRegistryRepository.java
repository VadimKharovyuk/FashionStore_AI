package com.example.fashionstore_ai.repository;


import com.example.fashionstore_ai.enums.AgentType;
import com.example.fashionstore_ai.model.AgentRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRegistryRepository extends JpaRepository<AgentRegistry, Long> {

    Optional<AgentRegistry> findByAgentType(AgentType agentType);

    List<AgentRegistry> findByIsActiveTrueOrderByPriorityDesc();

    // Agent RAG: семантичний пошук через pgvector
    // повертає топ-N агентів за косинусною схожістю до query embedding
    @Query(value = """
            SELECT * FROM agent_registry
            WHERE is_active = true
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<AgentRegistry> findTopByEmbeddingSimilarity(
            @Param("embedding") String embedding,
            @Param("limit")     int limit
    );
}
