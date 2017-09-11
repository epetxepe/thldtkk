package fi.thl.thldtkk.api.metadata.service.v3.termed;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.Maps.difference;
import static fi.thl.thldtkk.api.metadata.domain.query.AndCriteria.and;
import static fi.thl.thldtkk.api.metadata.domain.query.CriteriaUtils.keyWithAnyValue;
import static fi.thl.thldtkk.api.metadata.domain.query.KeyValueCriteria.keyValue;
import static fi.thl.thldtkk.api.metadata.domain.query.Select.select;
import static fi.thl.thldtkk.api.metadata.util.MapUtils.index;
import static fi.thl.thldtkk.api.metadata.util.Tokenizer.tokenizeAndMap;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

import fi.thl.thldtkk.api.metadata.domain.Dataset;
import fi.thl.thldtkk.api.metadata.domain.InstanceVariable;
import fi.thl.thldtkk.api.metadata.domain.NodeEntity;
import fi.thl.thldtkk.api.metadata.domain.Population;
import fi.thl.thldtkk.api.metadata.domain.query.Criteria;
import fi.thl.thldtkk.api.metadata.domain.termed.Changeset;
import fi.thl.thldtkk.api.metadata.domain.termed.Node;
import fi.thl.thldtkk.api.metadata.domain.termed.NodeId;
import fi.thl.thldtkk.api.metadata.service.v3.DatasetService;
import fi.thl.thldtkk.api.metadata.service.v3.Repository;
import fi.thl.thldtkk.api.metadata.util.spring.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DatasetServiceImpl implements DatasetService {

  private Repository<NodeId, Node> nodes;

  public DatasetServiceImpl(Repository<NodeId, Node> nodes) {
    this.nodes = nodes;
  }

  @Override
  public List<Dataset> findAll() {
    return nodes.query(keyValue("type.id", "DataSet"))
        .map(Dataset::new)
        .collect(toList());
  }

  @Override
  public List<Dataset> find(String query, int max) {
    return nodes.query(
        and(keyValue("type.id", "DataSet"),
            keyWithAnyValue("properties.prefLabel", tokenizeAndMap(query, t -> t + "*"))),
        max)
        .map(Dataset::new)
        .collect(toList());
  }

  @Override
  public List<Dataset> find(UUID organizationId, UUID datasetTypeId, String query, int max) {
    List<Criteria> criteria = new ArrayList<>();

    criteria.add(keyValue("type.id", "DataSet"));

    if (organizationId != null) {
      criteria.add(keyValue("references.organization.id", organizationId.toString()));
    }
    if (datasetTypeId != null) {
      criteria.add(keyValue("references.datasetType.id", datasetTypeId.toString()));
    }
    if (!query.isEmpty()) {
      List<String> tokens = tokenizeAndMap(query, t -> t + "*");
      criteria.add(keyWithAnyValue("properties.prefLabel", tokens));
    }

    return nodes.query(and(criteria), max).map(Dataset::new).collect(toList());
  }

  @Override
  public Optional<Dataset> get(UUID id) {
    return nodes.get(select("id", "type", "properties.*", "references.*",
        "references.inScheme:2",
        "references.conceptsFromScheme:2",
        "references.variable:2",
        "references.quantity:2",
        "references.unit:2",
        "references.codeList:2",
        "references.source:2",
        "references.instanceQuestions:2",
        "references.personInRoles:2",
        "references.person:2",
        "references.role:2"),
        new NodeId(id, "DataSet")).map(Dataset::new);
  }

  @Override
  public Dataset saveNotUpdatingInstanceVariables(Dataset dataset) {
    Optional<Dataset> optionalPrevDataset =
        dataset.getId() != null ? get(dataset.getId()) : Optional.empty();

    return save(optionalPrevDataset
        .map(prevDataset -> new Dataset(dataset, prevDataset.getInstanceVariables()))
        .orElse(dataset));
  }

  @Override
  public Dataset save(Dataset dataset) {
    Optional<Dataset> old;

    if (dataset.getId() == null) {
      old = empty();
      dataset.setId(randomUUID());
    } else {
      old = get(dataset.getId());
    }

    boolean isDatasetPublished = dataset.isPublished().orElse(false);

    dataset.getPopulation()
        .ifPresent(p -> p.setId(firstNonNull(p.getId(), randomUUID())));
    dataset.getInstanceVariables()
        .forEach(iv -> {
          iv.setId(firstNonNull(iv.getId(), randomUUID()));
          iv.setPublished(isDatasetPublished);
          if (InstanceVariable.VALUE_DOMAIN_TYPE_DESCRIBED
              .equals(iv.getValueDomainType().orElse(null))) {
            iv.setCodeList(null);
          } else if (InstanceVariable.VALUE_DOMAIN_TYPE_ENUMERATED
              .equals(iv.getValueDomainType().orElse(null))) {
            iv.setQuantity(null);
            iv.setUnit(null);
            iv.setValueRangeMax(null);
            iv.setValueRangeMin(null);
          }
        });
    dataset.getLinks()
        .forEach(v -> v.setId(firstNonNull(v.getId(), randomUUID())));
    dataset.getPersonInRoles()
        .forEach(pir -> pir.setId(firstNonNull(pir.getId(), randomUUID())));

    if (!old.isPresent()) {
      insert(dataset);
    } else {
      update(dataset, old.get());
    }

    return dataset;
  }

  private void insert(Dataset dataset) {
    List<Node> save = new ArrayList<>();

    save.add(dataset.toNode());
    dataset.getPopulation().ifPresent(v -> save.add(v.toNode()));
    dataset.getInstanceVariables().forEach(iv -> save.add(iv.toNode()));
    dataset.getLinks().forEach(v -> save.add(v.toNode()));
    dataset.getPersonInRoles().forEach(pir -> save.add(pir.toNode()));

    nodes.save(save);
  }

  private void update(Dataset newDataset, Dataset oldDataset) {
    Changeset<NodeId, Node> changeset = Changeset.<NodeId, Node>save(newDataset.toNode())
        .merge(buildChangeset(
            newDataset.getPopulation().orElse(null),
            oldDataset.getPopulation().orElse(null)))
        .merge(buildChangeset(
            newDataset.getInstanceVariables(),
            oldDataset.getInstanceVariables()))
        .merge(buildChangeset(
            newDataset.getLinks(),
            oldDataset.getLinks()))
        .merge(buildChangeset(
            newDataset.getPersonInRoles(),
            oldDataset.getPersonInRoles()));

    nodes.post(changeset);
  }

  private Changeset<NodeId, Node> buildChangeset(Population newPopulation,
      Population oldPopulation) {
    if (newPopulation != null && oldPopulation == null) {
      return Changeset.save(newPopulation.toNode());
    }
    // save with existing id
    if (newPopulation != null && !newPopulation.equals(oldPopulation)) {
      return Changeset.save(new Population(
          oldPopulation.getId(),
          newPopulation.getPrefLabel(),
          newPopulation.getDescription(),
          newPopulation.getSampleSize(),
          newPopulation.getLoss(),
          newPopulation.getGeographicalCoverage()).toNode());
    }
    if (newPopulation == null && oldPopulation != null) {
      return Changeset.delete(new NodeId(oldPopulation.toNode()));
    }
    return Changeset.empty();
  }

  private <T extends NodeEntity> Changeset<NodeId, Node> buildChangeset(
      List<T> newNodeEntities,
      List<T> oldNodeEntities) {

    Map<UUID, T> newNodeEntitiesById = index(newNodeEntities, T::getId);
    Map<UUID, T> oldNodeEntitiesById = index(oldNodeEntities, T::getId);

    List<NodeId> deleted = difference(newNodeEntitiesById, oldNodeEntitiesById)
        .entriesOnlyOnRight()
        .values().stream().map(T::toNode).map(NodeId::new)
        .collect(toList());

    List<Node> saved = newNodeEntities.stream()
        .map(NodeEntity::toNode).collect(toList());

    return new Changeset<>(deleted, saved);
  }

  @Override
  public InstanceVariable saveDatasetInstanceVariable(UUID datasetId,
      InstanceVariable instanceVariable) {

    Dataset dataset = get(datasetId).orElseThrow(NotFoundException::new);

    UUID instanceVariableId = firstNonNull(instanceVariable.getId(), randomUUID());
    instanceVariable.setId(instanceVariableId);

    Map<UUID, InstanceVariable> variablesById
        = index(dataset.getInstanceVariables(), InstanceVariable::getId);
    variablesById.put(instanceVariable.getId(), instanceVariable);

    dataset = save(new Dataset(dataset, new ArrayList<>(variablesById.values())));

    return dataset.getInstanceVariables().stream()
        .filter(iv -> instanceVariableId.equals(iv.getId()))
        .findFirst().orElseThrow(IllegalStateException::new);
  }

  @Override
  public void delete(UUID id) {
    Dataset dataset = get(id).orElseThrow(NotFoundException::new);

    List<Node> delete = new ArrayList<>();
    delete.add(dataset.toNode());

    // delete dependent nodes
    dataset.getPopulation().ifPresent(v -> delete.add(v.toNode()));
    dataset.getInstanceVariables().forEach(v -> delete.add(v.toNode()));
    dataset.getLinks().forEach(v -> delete.add(v.toNode()));
    dataset.getPersonInRoles().forEach(pir -> delete.add(pir.toNode()));

    nodes.delete(delete.stream().map(NodeId::new).collect(toList()));
  }

  @Override
  public void deleteDatasetInstanceVariable(UUID datasetId, UUID instanceVariableId) {
    Dataset dataset = get(datasetId).orElseThrow(NotFoundException::new);

    List<InstanceVariable> variables = new ArrayList<>();
    variables.addAll(dataset.getInstanceVariables());
    variables.removeIf(v -> v.getId().equals(instanceVariableId));

    save(new Dataset(dataset, variables));
  }

}