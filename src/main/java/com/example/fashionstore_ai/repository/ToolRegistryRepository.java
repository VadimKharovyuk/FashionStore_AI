package com.example.fashionstore_ai.repository;
import com.example.fashionstore_ai.enums.AgentType;
import com.example.fashionstore_ai.model.ToolRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToolRegistryRepository extends JpaRepository<ToolRegistry, Long> {

    Optional<ToolRegistry> findByToolName(String toolName);

    // всі активні tools конкретного агента
    List<ToolRegistry> findByOwnerAgentAndIsActiveTrueOrderByToolNameAsc(AgentType ownerAgent);

    // Tool RAG: семантичний пошук серед tools конкретного агента
    @Query(value = """
            SELECT * FROM tool_registry
            WHERE owner_agent = :ownerAgent
              AND is_active = true
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<ToolRegistry> findTopByOwnerAgentAndEmbeddingSimilarity(
            @Param("ownerAgent") String ownerAgent,
            @Param("embedding")  String embedding,
            @Param("limit")      int limit
    );

    // tools що потребують підтвердження — для SupportAgent
    List<ToolRegistry> findByOwnerAgentAndRequiresConfirmationTrue(AgentType ownerAgent);
}
