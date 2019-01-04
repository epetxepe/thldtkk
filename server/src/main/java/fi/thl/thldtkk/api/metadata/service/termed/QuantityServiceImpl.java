package fi.thl.thldtkk.api.metadata.service.termed;

import static fi.thl.thldtkk.api.metadata.domain.query.AndCriteria.and;
import static fi.thl.thldtkk.api.metadata.domain.query.CriteriaUtils.keyWithAllValues;
import static fi.thl.thldtkk.api.metadata.domain.query.KeyValueCriteria.keyValue;
import static fi.thl.thldtkk.api.metadata.util.Tokenizer.tokenizeAndMap;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

import fi.thl.thldtkk.api.metadata.domain.Quantity;
import fi.thl.thldtkk.api.metadata.domain.query.KeyValueCriteria;
import fi.thl.thldtkk.api.metadata.domain.termed.Node;
import fi.thl.thldtkk.api.metadata.domain.termed.NodeId;
import fi.thl.thldtkk.api.metadata.security.annotation.UserCanCreateAdminCanUpdate;
import fi.thl.thldtkk.api.metadata.service.QuantityService;
import fi.thl.thldtkk.api.metadata.service.Repository;
import org.springframework.security.access.method.P;

import java.util.*;

public class QuantityServiceImpl implements QuantityService {

  private Repository<NodeId, Node> nodes;

  public QuantityServiceImpl(Repository<NodeId, Node> nodes) {
    this.nodes = nodes;
  }

  @Override
  public List<Quantity> findAll() {
    return nodes.query(keyValue("type.id", "Quantity"))
        .map(Quantity::new)
        .collect(toList());
  }

  @Override
  public List<Quantity> find(String query, int max) {
    return nodes.query(
        and(keyValue("type.id", "Quantity"),
            keyWithAllValues("properties.prefLabel", tokenizeAndMap(query, t -> t + "*"))),
        max)
        .map(Quantity::new)
        .collect(toList());
  }

  @Override
  public Optional<Quantity> get(UUID id) {
    return nodes.get(new NodeId(id, "Quantity")).map(Quantity::new);
  }

  @UserCanCreateAdminCanUpdate
  @Override
  public Quantity save(@P("entity") Quantity quantity) {
    return new Quantity(nodes.save(quantity.toNode()));
  }

  @Override
  public Optional<Quantity> findByPrefLabel(String prefLabel) {
    if (prefLabel == null || prefLabel.isEmpty()) {
      return Optional.empty();
    }

    prefLabel = "\"" + prefLabel + "\"";

    return nodes.query(
	    and(keyValue("type.id", "Quantity"), keyValue(
                    "properties.prefLabel",
                    prefLabel)),
            1)
            .map(Quantity::new)
            .findFirst();
  }
}
