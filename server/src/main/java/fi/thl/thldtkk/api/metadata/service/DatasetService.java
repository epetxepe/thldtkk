package fi.thl.thldtkk.api.metadata.service;

import fi.thl.thldtkk.api.metadata.domain.Dataset;
import fi.thl.thldtkk.api.metadata.domain.InstanceVariable;
import fi.thl.thldtkk.api.metadata.domain.Link;
import fi.thl.thldtkk.api.metadata.domain.Population;
import fi.thl.thldtkk.api.metadata.domain.termed.Changeset;
import fi.thl.thldtkk.api.metadata.domain.termed.Node;
import fi.thl.thldtkk.api.metadata.domain.termed.NodeId;
import fi.thl.thldtkk.api.metadata.util.spring.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.Maps.difference;
import static fi.thl.thldtkk.api.metadata.util.MapUtils.index;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

/**
 * Dataset service that converts Datasets to nodes and back, and delegates
 * actual CRUD operations to given node service.
 */
@Component
public class DatasetService implements Service<UUID, Dataset> {

    private TermedNodeService nodeService;

    @Autowired
    public DatasetService(TermedNodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public Stream<Dataset> query() {
        return query("");
    }

    @Override
    public Stream<Dataset> query(String query) {
      String nodeQuery = "type.id:DataSet" + (query.isEmpty() ? "" : " AND (" + query + ")");
      return nodeService.query(
        new NodeRequestBuilder()
          .withQuery(nodeQuery)
          .addSort("prefLabel")
          .build()
      ).map(Dataset::new);
    }

    @Override
    public Optional<Dataset> get(UUID id) {
        return nodeService.get(new NodeId(id, "DataSet"),
            "id,type,properties.*,references.*,references.inScheme:2,references.conceptsFromScheme:2,references.variable:2,references.quantity:2,references.unit:2,references.codeList:2,references.source:2,references.instanceQuestions:2").map(Dataset::new);
    }

    @Override
    public Dataset save(Dataset dataset) {
        Optional<Dataset> old;
        if (dataset.getId() == null) {
          old = empty();
          dataset.setId(randomUUID());
        }
        else {
          old = get(dataset.getId());
        }

        dataset.getPopulation()
                .ifPresent(p -> p.setId(firstNonNull(p.getId(), randomUUID())));
        dataset.getInstanceVariables()
                .forEach(iv -> {
                  iv.setId(firstNonNull(iv.getId(), randomUUID()));
                  if (InstanceVariable.VALUE_DOMAIN_TYPE_DESCRIBED.equals(iv.getValueDomainType().orElse(null))) {
                    iv.setCodeList(null);
                  }
                  else if (InstanceVariable.VALUE_DOMAIN_TYPE_ENUMERATED.equals(iv.getValueDomainType().orElse(null))) {
                    iv.setQuantity(null);
                    iv.setUnit(null);
                    iv.setValueRangeMax(null);
                    iv.setValueRangeMin(null);
                  }
                });
        dataset.getLinks()
                .forEach(v -> v.setId(firstNonNull(v.getId(), randomUUID())));

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

        nodeService.save(save);
    }

    private void update(Dataset newDataset, Dataset oldDataset) {
        Changeset<NodeId, Node> changeset = Changeset.<NodeId, Node>save(
                newDataset.toNode())
                .merge(buildChangeset(
                        newDataset.getPopulation().orElse(null),
                        oldDataset.getPopulation().orElse(null)))
                .merge(buildChangeset(
                        newDataset.getInstanceVariables(),
                        oldDataset.getInstanceVariables()))
                .merge(buildLinkChangeset(
                        newDataset.getLinks(),
                        oldDataset.getLinks()));

        nodeService.post(changeset);
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

    private Changeset<NodeId, Node> buildLinkChangeset(List<Link> newLinks,
            List<Link> oldLinks) {
        Map<UUID, Link> newLinksById
                = index(newLinks, Link::getId);
        Map<UUID, Link> oldLinksById
                = index(oldLinks, Link::getId);

        List<NodeId> deleted = difference(newLinksById, oldLinksById).entriesOnlyOnRight()
                .values().stream().map(Link::toNode).map(NodeId::new)
                .collect(toList());

        List<Node> saved = newLinks.stream()
                .map(Link::toNode).collect(toList());

        return new Changeset<>(deleted, saved);
    }

    private Changeset<NodeId, Node> buildChangeset(
            List<InstanceVariable> newInstanceVariables,
            List<InstanceVariable> oldInstanceVariables) {

        Map<UUID, InstanceVariable> newVarsById
                = index(newInstanceVariables, InstanceVariable::getId);
        Map<UUID, InstanceVariable> oldVarsById
                = index(oldInstanceVariables, InstanceVariable::getId);

        List<NodeId> deleted = difference(newVarsById, oldVarsById).entriesOnlyOnRight()
                .values().stream().map(InstanceVariable::toNode).map(NodeId::new)
                .collect(toList());

        List<Node> saved = newInstanceVariables.stream()
                .map(InstanceVariable::toNode).collect(toList());

        return new Changeset<>(deleted, saved);
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

        nodeService.delete(delete.stream().map(NodeId::new).collect(toList()));
    }

    public Stream<Dataset> getRecentAndPublished(int maxResults) {
      String nodeQuery = "type.id:DataSet AND properties.published:true*";
      Comparator<Date> dateComparator = (date, anotherDate) -> date.compareTo(anotherDate);

      return nodeService.query(
        new NodeRequestBuilder()
          .addDefaultIncludedAttributes()
          .addIncludedAttribute("lastModifiedDate")
          .withQuery(nodeQuery)
          .build()
      ).sorted(Comparator.comparing(Node::getLastModifiedDate, dateComparator).reversed())
       .limit(maxResults)
       .map(Dataset::new);
    }
}
