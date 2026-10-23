package com.bembosevents.ai.knowledge;

public interface KnowledgeRetriever {

    KnowledgeRetrievalResult recuperar(
            KnowledgeQuery query);
}