package fi.thl.thldtkk.api.metadata.service.termed;

import fi.thl.thldtkk.api.metadata.domain.Dataset;
import fi.thl.thldtkk.api.metadata.domain.query.KeyValueCriteria;
import fi.thl.thldtkk.api.metadata.domain.termed.Node;
import fi.thl.thldtkk.api.metadata.domain.termed.NodeId;
import fi.thl.thldtkk.api.metadata.security.annotation.AdminOnly;
import fi.thl.thldtkk.api.metadata.service.EditorDatasetService;
import fi.thl.thldtkk.api.metadata.service.EditorStudyService;
import fi.thl.thldtkk.api.metadata.service.Repository;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import static fi.thl.thldtkk.api.metadata.domain.query.AndCriteria.and;
import static fi.thl.thldtkk.api.metadata.domain.query.KeyValueCriteria.keyValue;
import static fi.thl.thldtkk.api.metadata.domain.query.Select.select;
import static java.util.stream.Collectors.toList;

public class EditorDatasetServiceImpl implements EditorDatasetService {

  private Repository<NodeId, Node> nodes;
  private EditorStudyService editorStudyService;

  private static final int COMPARE_UNDETERMINED = 0;
  private static final String DEFAULT_LANGUAGE = "fi";
  
  public EditorDatasetServiceImpl(Repository<NodeId, Node> nodes, EditorStudyService editorStudyService) {
    this.nodes = nodes;
    this.editorStudyService = editorStudyService;
  }

  @Override
  public List<Dataset> findAll() {
    return findAll(DEFAULT_LANGUAGE);
  }

  @Override
  public List<Dataset> findAll(String language) {
    return editorStudyService.findAll()
            .stream()
            .map(study -> study.getDatasets())
            .flatMap(datasets -> datasets.stream())
            .sorted(Comparator.comparing(Dataset::getPrefLabel, (aPrefLabel, anotherPrefLabel) -> {
              boolean areLabelsComparable = StringUtils.hasText(language) 
                      && aPrefLabel.containsKey(language) 
                      && anotherPrefLabel.containsKey(language);
              
              return areLabelsComparable
                      ? aPrefLabel.get(language).toLowerCase().compareTo(anotherPrefLabel.get(language).toLowerCase())
                      : COMPARE_UNDETERMINED;
            }))
            .collect(Collectors.toList());
  }
  
  @AdminOnly
  @Override
  public List<Dataset> getDatasetsByUnitType(UUID unitTypeId) {
    return nodes.query(
      select("id", "type", "properties.*", "references.*"),
      and(keyValue("type.id", Dataset.TERMED_NODE_CLASS),
        keyValue("references.unitType.id", unitTypeId.toString())))
      .map(Dataset::new).collect(Collectors.toList());
  }

  @AdminOnly
  @Override
  public List<Dataset> getUniverseDatasets(UUID universeId){
    List<Dataset> list =  nodes.query(
            select("id", "type", "properties.*", "references.*", "referrers.*"),
            and(keyValue("type.id", Dataset.TERMED_NODE_CLASS),
                    keyValue("references.universe.id", universeId.toString())))
            .map(Dataset::new)
            .collect(toList());
    return list;
  }

  @Override
  public List<Dataset> find(String query, int max) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<Dataset> get(UUID id) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<Dataset> findByPrefLabel(String prefLabel) {
    if (prefLabel == null || prefLabel.isEmpty()) {
      return Optional.empty();
    }

    prefLabel = "\"" + prefLabel + "\"";

    return nodes.query(
            and(keyValue("type.id", Dataset.TERMED_NODE_CLASS), keyValue(
                    "properties.prefLabel",
                    prefLabel)),
            1)
            .map(Dataset::new)
            .findFirst();
  }

}
