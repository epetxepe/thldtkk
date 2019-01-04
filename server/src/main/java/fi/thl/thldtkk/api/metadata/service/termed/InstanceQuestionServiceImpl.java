package fi.thl.thldtkk.api.metadata.service.termed;

import static fi.thl.thldtkk.api.metadata.domain.query.AndCriteria.and;
import static fi.thl.thldtkk.api.metadata.domain.query.CriteriaUtils.keyWithAnyValue;
import static fi.thl.thldtkk.api.metadata.domain.query.KeyValueCriteria.keyValue;
import static fi.thl.thldtkk.api.metadata.domain.query.Select.select;
import static fi.thl.thldtkk.api.metadata.domain.query.Sort.sort;
import static fi.thl.thldtkk.api.metadata.util.Tokenizer.tokenizeAndMap;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import fi.thl.thldtkk.api.metadata.domain.Dataset;
import fi.thl.thldtkk.api.metadata.domain.InstanceQuestion;
import fi.thl.thldtkk.api.metadata.domain.query.KeyValueCriteria;
import fi.thl.thldtkk.api.metadata.domain.termed.Node;
import fi.thl.thldtkk.api.metadata.domain.termed.NodeId;
import fi.thl.thldtkk.api.metadata.security.annotation.UserCanCreateAdminCanUpdate;
import fi.thl.thldtkk.api.metadata.service.InstanceQuestionService;
import fi.thl.thldtkk.api.metadata.service.Repository;
import fi.thl.thldtkk.api.metadata.util.spring.exception.NotFoundException;

import java.util.*;
import java.util.function.Supplier;
import fi.thl.thldtkk.api.metadata.service.EditorStudyService;

public class InstanceQuestionServiceImpl implements InstanceQuestionService {

  private final EditorStudyService studyService;
  private final Repository<NodeId, Node> nodes;

  public InstanceQuestionServiceImpl(EditorStudyService studyService,
                                     Repository<NodeId, Node> nodes) {
    this.studyService = studyService;
    this.nodes = nodes;
  }

  @Override
  public List<InstanceQuestion> findAll() {
    return nodes.query(keyValue("type.id", "InstanceQuestion"))
        .map(InstanceQuestion::new)
        .collect(toList());
  }

  @Override
  public List<InstanceQuestion> find(String query, int max) {
    return nodes.query(
        and(keyValue("type.id", "InstanceQuestion"),
            keyWithAnyValue("properties.prefLabel", tokenizeAndMap(query, t -> t + "*"))),
        max)
        .map(InstanceQuestion::new)
        .collect(toList());
  }

  @Override
  public List<InstanceQuestion> findDatasetInstanceQuestions(UUID studyId, UUID datasetId, String query) {
    Dataset dataset = studyService.getDataset(studyId, datasetId).orElseThrow(
      (Supplier<RuntimeException>) () -> new NotFoundException("Dataset " + datasetId));

    Set<UUID> datasetQuestionIds = dataset.getInstanceVariables().stream()
        .flatMap(variable -> variable.getInstanceQuestions().stream())
        .map(InstanceQuestion::getId).collect(toSet());

    return nodes.query(
        select("id", "type", "properties.*", "references.*"),
        and(keyValue("type.id", "InstanceQuestion"),
            keyWithAnyValue("properties.prefLabel", tokenizeAndMap(query, t -> t + "*"))),
        sort("properties.prefLabel.sortable"))
        .map(InstanceQuestion::new)
        .filter(instanceQuestion -> datasetQuestionIds.contains(instanceQuestion.getId()))
        .collect(toList());
  }

  @Override
  public Optional<InstanceQuestion> get(UUID id) {
    return nodes.get(new NodeId(id, "InstanceQuestion")).map(InstanceQuestion::new);
  }
  
  @Override
  public InstanceQuestion save(InstanceQuestion instanceQuestion) {
    return new InstanceQuestion(nodes.save(instanceQuestion.toNode()));
  }

  @Override
  public Optional<InstanceQuestion> findByPrefLabel(String prefLabel) {
    if (prefLabel == null || prefLabel.isEmpty()) {
      return Optional.empty();
    }

    prefLabel = "\"" + prefLabel + "\"";

    return nodes.query(
            and(keyValue("type.id", "InstanceQuestion"), keyValue(
                    "properties.prefLabel",
                    prefLabel)),
            1)
            .map(InstanceQuestion::new)
            .findFirst();
  }
}
